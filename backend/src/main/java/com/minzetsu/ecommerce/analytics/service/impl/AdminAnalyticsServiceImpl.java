package com.minzetsu.ecommerce.analytics.service.impl;

import com.minzetsu.ecommerce.analytics.dto.response.AdminFunnelAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.dto.response.AdminTopProductAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.repository.DailyProductMetricRepository;
import com.minzetsu.ecommerce.analytics.repository.projection.FunnelAggregateView;
import com.minzetsu.ecommerce.analytics.repository.projection.TopProductAggregateView;
import com.minzetsu.ecommerce.analytics.service.AdminAnalyticsService;
import com.minzetsu.ecommerce.analytics.service.AnalyticsRealtimeCounterService;
import com.minzetsu.ecommerce.common.exception.InvalidObjectException;
import com.minzetsu.ecommerce.mongo.repository.ClickstreamEventRepository;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private static final String EVENT_PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
    private final DailyProductMetricRepository dailyProductMetricRepository;
    private final AnalyticsRealtimeCounterService analyticsRealtimeCounterService;
    private final ProductRepository productRepository;
    private final ClickstreamEventRepository clickstreamEventRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analyticsAdmin", key = "'funnel:' + #from + ':' + #to", sync = true)
    public AdminFunnelAnalyticsResponse getFunnel(LocalDate from, LocalDate to) {
        validateRange(from, to);
        FunnelSnapshot current = loadFunnelSnapshot(from, to);
        LocalDate previousTo = from.minusDays(1);
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate previousFrom = previousTo.minusDays(days - 1);
        FunnelSnapshot previous = loadFunnelSnapshot(previousFrom, previousTo);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        FunnelSnapshot todaySnapshot = loadFunnelSnapshot(today, today);

        return AdminFunnelAnalyticsResponse.builder()
                .from(from)
                .to(to)
                .views(current.views())
                .addToCart(current.addToCart())
                .orders(current.orders())
                .paymentSuccess(current.paymentSuccess())
                .viewToCartRate(ratio(current.addToCart(), current.views()))
                .cartToOrderRate(ratio(current.orders(), current.addToCart()))
                .orderToPaymentRate(ratio(current.paymentSuccess(), current.orders()))
                .viewToOrderRate(ratio(current.orders(), current.views()))
                .previousFrom(previousFrom)
                .previousTo(previousTo)
                .previousViews(previous.views())
                .previousAddToCart(previous.addToCart())
                .previousOrders(previous.orders())
                .previousPaymentSuccess(previous.paymentSuccess())
                .previousViewToOrderRate(ratio(previous.orders(), previous.views()))
                .viewsChangeRate(changeRate(current.views(), previous.views()))
                .addToCartChangeRate(changeRate(current.addToCart(), previous.addToCart()))
                .ordersChangeRate(changeRate(current.orders(), previous.orders()))
                .viewToOrderRateChange(changeRate(ratio(current.orders(), current.views()), ratio(previous.orders(), previous.views())))
                .todayViews(todaySnapshot.views())
                .todayAddToCart(todaySnapshot.addToCart())
                .todayOrders(todaySnapshot.orders())
                .todayPaymentSuccess(todaySnapshot.paymentSuccess())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analyticsAdmin", key = "'top-products:' + #from + ':' + #to + ':limit:' + #limit", sync = true)
    public List<AdminTopProductAnalyticsResponse> getTopProducts(LocalDate from, LocalDate to, int limit) {
        validateRange(from, to);
        int boundedLimit = Math.max(1, Math.min(limit, 100));
        DateRangeSplit split = splitRange(from, to);
        Map<Long, MutableTopProduct> merged = new HashMap<>();

        if (split.hasHistoricalRange()) {
            List<TopProductAggregateView> rows =
                    dailyProductMetricRepository.findTopProductsByRange(split.historicalFrom(), split.historicalTo());
            for (TopProductAggregateView row : rows) {
                Long productId = row.getProductId();
                if (productId == null) {
                    continue;
                }
                MutableTopProduct item = merged.computeIfAbsent(productId, ignored -> new MutableTopProduct());
                item.productName = row.getProductName();
                item.views += valueOrZero(row.getViews());
                item.addToCart += valueOrZero(row.getAddToCart());
                item.orders += valueOrZero(row.getOrders());
                item.uniqueUsers += valueOrZero(row.getUniqueUsers());
            }
        }

        if (split.includesToday()) {
            Map<Long, AnalyticsRealtimeCounterService.RealtimeProductMetric> realtimeRows =
                    analyticsRealtimeCounterService.readTopProducts(split.today());
            Map<Long, String> realtimeNames = resolveProductNames(realtimeRows.keySet());
            for (Map.Entry<Long, AnalyticsRealtimeCounterService.RealtimeProductMetric> entry : realtimeRows.entrySet()) {
                Long productId = entry.getKey();
                AnalyticsRealtimeCounterService.RealtimeProductMetric metric = entry.getValue();
                MutableTopProduct item = merged.computeIfAbsent(productId, ignored -> new MutableTopProduct());
                if (item.productName == null || item.productName.isBlank()) {
                    item.productName = realtimeNames.get(productId);
                }
                item.views += metric.views();
                item.addToCart += metric.addToCart();
                item.orders += metric.orders();
                item.uniqueUsers += metric.uniqueUsers();
            }
        }

        return merged.entrySet().stream()
                .map(entry -> {
                    Long productId = entry.getKey();
                    MutableTopProduct row = entry.getValue();
                    return AdminTopProductAnalyticsResponse.builder()
                            .productId(productId)
                            .productName(row.productName)
                            .views(row.views)
                            .addToCart(row.addToCart)
                            .orders(row.orders)
                            .uniqueUsers(row.uniqueUsers)
                            .conversionRate(ratio(row.orders, row.views))
                            .build();
                })
                .sorted(
                        Comparator.comparing(AdminTopProductAnalyticsResponse::getConversionRate).reversed()
                                .thenComparing(AdminTopProductAnalyticsResponse::getOrders, Comparator.reverseOrder())
                                .thenComparing(AdminTopProductAnalyticsResponse::getViews, Comparator.reverseOrder())
                )
                .limit(boundedLimit)
                .toList();
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal ratio(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal changeRate(long current, long previous) {
        if (previous <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(current - previous)
                .divide(BigDecimal.valueOf(previous), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal changeRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new InvalidObjectException("from and to are required");
        }
        if (from.isAfter(to)) {
            throw new InvalidObjectException("from must be less than or equal to to");
        }
    }

    private DateRangeSplit splitRange(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate historicalTo = to.isBefore(today) ? to : today.minusDays(1);
        boolean hasHistoricalRange = !from.isAfter(historicalTo);
        boolean includesToday = !from.isAfter(today) && !to.isBefore(today);
        return new DateRangeSplit(from, historicalTo, hasHistoricalRange, includesToday, today);
    }

    private Map<Long, String> resolveProductNames(java.util.Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        List<Product> products = productRepository.findAllById(productIds);
        Map<Long, String> names = new HashMap<>();
        for (Product product : products) {
            names.put(product.getId(), product.getName());
        }
        return names;
    }

    private FunnelSnapshot loadFunnelSnapshot(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            return FunnelSnapshot.empty();
        }
        DateRangeSplit split = splitRange(from, to);
        long views = 0L;
        long addToCart = 0L;
        long orders = 0L;
        if (split.hasHistoricalRange()) {
            FunnelAggregateView aggregate =
                    dailyProductMetricRepository.aggregateFunnel(split.historicalFrom(), split.historicalTo());
            views += valueOrZero(aggregate.getViews());
            addToCart += valueOrZero(aggregate.getAddToCart());
            orders += valueOrZero(aggregate.getOrders());
        }
        if (split.includesToday()) {
            AnalyticsRealtimeCounterService.RealtimeFunnel realtime =
                    analyticsRealtimeCounterService.readFunnel(split.today());
            views += realtime.views();
            addToCart += realtime.addToCart();
            orders += realtime.orders();
        }
        long paymentSuccess = loadPaymentSuccessCount(from, to);
        return new FunnelSnapshot(views, addToCart, orders, paymentSuccess);
    }

    private long loadPaymentSuccessCount(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        return clickstreamEventRepository.countByEventTypeAndEventTimeRange(
                EVENT_PAYMENT_SUCCESS,
                start,
                end
        );
    }

    private record DateRangeSplit(
            LocalDate historicalFrom,
            LocalDate historicalTo,
            boolean hasHistoricalRange,
            boolean includesToday,
            LocalDate today
    ) {}

    private record FunnelSnapshot(long views, long addToCart, long orders, long paymentSuccess) {
        private static FunnelSnapshot empty() {
            return new FunnelSnapshot(0L, 0L, 0L, 0L);
        }
    }

    private static class MutableTopProduct {
        private String productName;
        private long views;
        private long addToCart;
        private long orders;
        private long uniqueUsers;
    }
}

