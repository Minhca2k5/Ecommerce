package com.minzetsu.ecommerce.analytics.service;

import com.minzetsu.ecommerce.analytics.entity.DailyProductMetric;
import com.minzetsu.ecommerce.analytics.repository.DailyProductMetricRepository;
import com.minzetsu.ecommerce.mongo.ClickstreamEventDocument;
import com.minzetsu.ecommerce.mongo.ClickstreamEventRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsEtlServiceTest {

    @Mock
    private ClickstreamEventRepository clickstreamEventRepository;
    @Mock
    private DailyProductMetricRepository dailyProductMetricRepository;

    private AnalyticsEtlService analyticsEtlService;

    @BeforeEach
    void setUp() {
        analyticsEtlService = new AnalyticsEtlService(
                clickstreamEventRepository,
                dailyProductMetricRepository,
                new SimpleMeterRegistry()
        );
        ReflectionTestUtils.setField(analyticsEtlService, "etlEnabled", true);
        ReflectionTestUtils.setField(analyticsEtlService, "failureThreshold", 3);
        ReflectionTestUtils.setField(analyticsEtlService, "staleDaysThreshold", 2);
    }

    @Test
    void runEtlForDate_shouldAggregateProductMetricsAndPersist() {
        LocalDate targetDate = LocalDate.of(2026, 2, 14);
        LocalDateTime t = targetDate.atTime(10, 0);
        ClickstreamEventDocument view1 = event("VIEW_PRODUCT", t, 101L, 1L, null);
        ClickstreamEventDocument view2 = event("VIEW_PRODUCT", t.plusMinutes(1), 101L, 1L, null);
        ClickstreamEventDocument add = event("ADD_TO_CART", t.plusMinutes(2), 101L, 1L, null);
        ClickstreamEventDocument order = event("PLACE_ORDER", t.plusMinutes(3), 101L, 1L, null);

        when(clickstreamEventRepository.findByEventTimeInRange(any(), any()))
                .thenReturn(List.of(view1, view2, add, order));
        when(dailyProductMetricRepository.deleteByMetricDate(targetDate)).thenReturn(0);

        analyticsEtlService.runEtlForDate(targetDate);

        ArgumentCaptor<List<DailyProductMetric>> rowsCaptor = ArgumentCaptor.forClass(List.class);
        verify(dailyProductMetricRepository).saveAll(rowsCaptor.capture());
        List<DailyProductMetric> rows = rowsCaptor.getValue();
        assertThat(rows).hasSize(1);
        DailyProductMetric row = rows.get(0);
        assertThat(row.getId().getMetricDate()).isEqualTo(targetDate);
        assertThat(row.getId().getProductId()).isEqualTo(101L);
        assertThat(row.getViews()).isEqualTo(2);
        assertThat(row.getAddToCart()).isEqualTo(1);
        assertThat(row.getOrders()).isEqualTo(1);
        assertThat(row.getUniqueUsers()).isEqualTo(1);
        assertThat(row.getConversionRate()).isEqualByComparingTo("0.5000");
    }

    @Test
    void runEtlForDate_shouldBeIdempotentWhenRerunSameDate() {
        LocalDate targetDate = LocalDate.of(2026, 2, 14);
        LocalDateTime t = targetDate.atTime(11, 0);
        ClickstreamEventDocument view = event("VIEW_PRODUCT", t, 201L, 2L, null);
        ClickstreamEventDocument order = event("PLACE_ORDER", t.plusMinutes(1), 201L, 2L, null);

        when(clickstreamEventRepository.findByEventTimeInRange(any(), any()))
                .thenReturn(List.of(view, order));
        when(dailyProductMetricRepository.deleteByMetricDate(eq(targetDate))).thenReturn(1);

        analyticsEtlService.runEtlForDate(targetDate);
        analyticsEtlService.runEtlForDate(targetDate);

        verify(dailyProductMetricRepository, times(2)).deleteByMetricDate(targetDate);
        verify(dailyProductMetricRepository, times(2)).saveAll(any());
    }

    @Test
    void runDailyEtl_shouldReadLatestMetricDateForStaleWindowCheck() {
        LocalDate expectedTarget = LocalDate.now(java.time.ZoneOffset.UTC).minusDays(1);
        when(clickstreamEventRepository.findByEventTimeInRange(any(), any()))
                .thenReturn(List.of());
        when(dailyProductMetricRepository.deleteByMetricDate(expectedTarget)).thenReturn(0);
        when(dailyProductMetricRepository.findLatestMetricDate()).thenReturn(Optional.of(expectedTarget));

        analyticsEtlService.runDailyEtl();

        verify(dailyProductMetricRepository).findLatestMetricDate();
    }

    @Test
    void runEtlForDate_shouldFailFastWhenPlaceOrderMissingProductId() {
        LocalDate targetDate = LocalDate.of(2026, 2, 15);
        LocalDateTime t = targetDate.atTime(9, 0);
        ClickstreamEventDocument view = event("VIEW_PRODUCT", t, 301L, 3L, null);
        ClickstreamEventDocument add = event("ADD_TO_CART", t.plusMinutes(1), 301L, 3L, null);
        ClickstreamEventDocument invalidPlaceOrder = event("PLACE_ORDER", t.plusMinutes(2), null, 3L, null);

        when(clickstreamEventRepository.findByEventTimeInRange(any(), any()))
                .thenReturn(List.of(view, add, invalidPlaceOrder));

        assertThatThrownBy(() -> analyticsEtlService.runEtlForDate(targetDate))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing_product_key=1");

        verify(dailyProductMetricRepository, never()).saveAll(any());
        verify(dailyProductMetricRepository, never()).deleteByMetricDate(any());
        verify(dailyProductMetricRepository, never()).findLatestMetricDate();
    }

    private ClickstreamEventDocument event(String eventType, LocalDateTime eventTime, Long productId, Long userId, String guestId) {
        ClickstreamEventDocument doc = new ClickstreamEventDocument();
        doc.setEventType(eventType);
        doc.setEventTime(eventTime);
        doc.setCreatedAt(eventTime);
        doc.setProductId(productId);
        doc.setUserId(userId);
        doc.setGuestId(guestId);
        doc.setRequestId("req-" + eventType + "-" + productId);
        doc.setSource("WEB");
        doc.setSchemaVersion("v1");
        return doc;
    }
}
