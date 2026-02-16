package com.minzetsu.ecommerce.analytics.service.impl;

import com.minzetsu.ecommerce.analytics.dto.response.AdminFunnelAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.dto.response.AdminTopProductAnalyticsResponse;
import com.minzetsu.ecommerce.analytics.repository.DailyProductMetricRepository;
import com.minzetsu.ecommerce.analytics.repository.projection.FunnelAggregateView;
import com.minzetsu.ecommerce.analytics.repository.projection.TopProductAggregateView;
import com.minzetsu.ecommerce.analytics.service.AnalyticsRealtimeCounterService;
import com.minzetsu.ecommerce.product.entity.Product;
import com.minzetsu.ecommerce.product.repository.ProductRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAnalyticsServiceImplTest {

    @Mock
    private DailyProductMetricRepository dailyProductMetricRepository;
    @Mock
    private AnalyticsRealtimeCounterService analyticsRealtimeCounterService;
    @Mock
    private ProductRepository productRepository;

    @Test
    void getFunnel_shouldMergeHistoricalWithRealtimeToday() {
        AdminAnalyticsServiceImpl service = new AdminAnalyticsServiceImpl(
                dailyProductMetricRepository,
                analyticsRealtimeCounterService,
                productRepository
        );
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = today.minusDays(2);
        LocalDate to = today;

        FunnelAggregateView historical = mock(FunnelAggregateView.class);
        when(historical.getViews()).thenReturn(100L);
        when(historical.getAddToCart()).thenReturn(20L);
        when(historical.getOrders()).thenReturn(5L);
        when(dailyProductMetricRepository.aggregateFunnel(eq(from), eq(today.minusDays(1))))
                .thenReturn(historical);
        when(analyticsRealtimeCounterService.readFunnel(today))
                .thenReturn(new AnalyticsRealtimeCounterService.RealtimeFunnel(10L, 4L, 2L));

        AdminFunnelAnalyticsResponse response = service.getFunnel(from, to);

        assertThat(response.getViews()).isEqualTo(110L);
        assertThat(response.getAddToCart()).isEqualTo(24L);
        assertThat(response.getOrders()).isEqualTo(7L);
        assertThat(response.getViewToOrderRate()).isEqualByComparingTo("0.0636");
    }

    @Test
    void getTopProducts_shouldMergeHistoricalAndRealtimeRows() {
        AdminAnalyticsServiceImpl service = new AdminAnalyticsServiceImpl(
                dailyProductMetricRepository,
                analyticsRealtimeCounterService,
                productRepository
        );
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = today.minusDays(1);
        LocalDate to = today;

        TopProductAggregateView historical = mock(TopProductAggregateView.class);
        when(historical.getProductId()).thenReturn(1L);
        when(historical.getProductName()).thenReturn("Phone A");
        when(historical.getViews()).thenReturn(100L);
        when(historical.getAddToCart()).thenReturn(25L);
        when(historical.getOrders()).thenReturn(10L);
        when(historical.getUniqueUsers()).thenReturn(40L);
        when(dailyProductMetricRepository.findTopProductsByRange(eq(from), eq(today.minusDays(1))))
                .thenReturn(List.of(historical));

        when(analyticsRealtimeCounterService.readTopProducts(today)).thenReturn(Map.of(
                1L, new AnalyticsRealtimeCounterService.RealtimeProductMetric(10L, 3L, 2L, 5L),
                2L, new AnalyticsRealtimeCounterService.RealtimeProductMetric(8L, 2L, 1L, 3L)
        ));

        Product p1 = Product.builder().name("Phone A").build();
        p1.setId(1L);
        Product p2 = Product.builder().name("Tablet B").build();
        p2.setId(2L);
        when(productRepository.findAllById(any())).thenReturn(List.of(p1, p2));

        List<AdminTopProductAnalyticsResponse> response = service.getTopProducts(from, to, 10);

        assertThat(response).hasSize(2);
        AdminTopProductAnalyticsResponse resultP1 = response.stream()
                .filter(r -> Long.valueOf(1L).equals(r.getProductId()))
                .findFirst()
                .orElseThrow();
        AdminTopProductAnalyticsResponse resultP2 = response.stream()
                .filter(r -> Long.valueOf(2L).equals(r.getProductId()))
                .findFirst()
                .orElseThrow();

        assertThat(resultP1.getViews()).isEqualTo(110L);
        assertThat(resultP1.getOrders()).isEqualTo(12L);
        assertThat(resultP1.getUniqueUsers()).isEqualTo(45L);

        assertThat(resultP2.getProductName()).isEqualTo("Tablet B");
        assertThat(resultP2.getViews()).isEqualTo(8L);
        assertThat(resultP2.getOrders()).isEqualTo(1L);
    }
}
