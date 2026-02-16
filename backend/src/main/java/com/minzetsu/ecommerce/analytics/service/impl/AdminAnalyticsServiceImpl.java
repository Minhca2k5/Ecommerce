package com.minzetsu.ecommerce.analytics.service.impl;

import com.minzetsu.ecommerce.analytics.dto.response.AdminFunnelAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.dto.response.AdminTopProductAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.repository.DailyProductMetricRepository;
import com.minzetsu.ecommerce.analytics.repository.projection.FunnelAggregateView;
import com.minzetsu.ecommerce.analytics.repository.projection.TopProductAggregateView;
import com.minzetsu.ecommerce.analytics.service.AdminAnalyticsService;
import com.minzetsu.ecommerce.analytics.service.AnalyticsRealtimeCounterService;
import com.minzetsu.ecommerce.common.exception.InvalidObjectException;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
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

    private final DailyProductMetricRepository dailyProductMetricRepository;
    private final AnalyticsRealtimeCounterService analyticsRealtimeCounterService;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analyticsAdmin", key = "'funnel:' + #from + ':' + #to", sync = true)
    public AdminFunnelAnalyticsResponse getFunnel(LocalDate from, LocalDate to) {
        validateRange(from, to);
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

        return AdminFunnelAnalyticsResponse.builder()
                .from(from)
                .to(to)
                .views(views)
                .addToCart(addToCart)
                .orders(orders)
                .viewToCartRate(ratio(addToCart, views))
                .cartToOrderRate(ratio(orders, addToCart))
                .viewToOrderRate(ratio(orders, views))
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
                .sorted((a, b) -> b.getConversionRate().compareTo(a.getConversionRate()))
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

    private record DateRangeSplit(
            LocalDate historicalFrom,
            LocalDate historicalTo,
            boolean hasHistoricalRange,
            boolean includesToday,
            LocalDate today
    ) {}

    private static class MutableTopProduct {
        private String productName;
        private long views;
        private long addToCart;
        private long orders;
        private long uniqueUsers;
    }
}
