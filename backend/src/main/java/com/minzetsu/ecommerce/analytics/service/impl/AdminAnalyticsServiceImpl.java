package com.minzetsu.ecommerce.analytics.service.impl;

import com.minzetsu.ecommerce.analytics.dto.response.AdminFunnelAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.dto.response.AdminTopProductAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.repository.DailyProductMetricRepository;
import com.minzetsu.ecommerce.analytics.repository.projection.FunnelAggregateView;
import com.minzetsu.ecommerce.analytics.repository.projection.TopProductAggregateView;
import com.minzetsu.ecommerce.analytics.service.AdminAnalyticsService;
import com.minzetsu.ecommerce.common.exception.InvalidObjectException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminAnalyticsServiceImpl implements AdminAnalyticsService {

    private final DailyProductMetricRepository dailyProductMetricRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "analyticsAdmin", key = "'funnel:' + #from + ':' + #to", sync = true)
    public AdminFunnelAnalyticsResponse getFunnel(LocalDate from, LocalDate to) {
        validateRange(from, to);
        FunnelAggregateView aggregate = dailyProductMetricRepository.aggregateFunnel(from, to);
        long views = valueOrZero(aggregate.getViews());
        long addToCart = valueOrZero(aggregate.getAddToCart());
        long orders = valueOrZero(aggregate.getOrders());

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
        List<TopProductAggregateView> rows = dailyProductMetricRepository.findTopProductsByRange(from, to);
        return rows.stream()
                .map(row -> {
                    long views = valueOrZero(row.getViews());
                    long orders = valueOrZero(row.getOrders());
                    return AdminTopProductAnalyticsResponse.builder()
                            .productId(row.getProductId())
                            .productName(row.getProductName())
                            .views(views)
                            .addToCart(valueOrZero(row.getAddToCart()))
                            .orders(orders)
                            .uniqueUsers(valueOrZero(row.getUniqueUsers()))
                            .conversionRate(ratio(orders, views))
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
}
