package com.minzetsu.ecommerce.analytics.service;

import com.minzetsu.ecommerce.analytics.entity.DailyProductMetric;
import com.minzetsu.ecommerce.analytics.entity.DailyProductMetricId;
import com.minzetsu.ecommerce.analytics.repository.DailyProductMetricRepository;
import com.minzetsu.ecommerce.mongo.ClickstreamEventDocument;
import com.minzetsu.ecommerce.mongo.ClickstreamEventRepository;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsEtlService {

    private static final String EVENT_VIEW_PRODUCT = "VIEW_PRODUCT";
    private static final String EVENT_ADD_TO_CART = "ADD_TO_CART";
    private static final String EVENT_PLACE_ORDER = "PLACE_ORDER";

    private final ClickstreamEventRepository clickstreamEventRepository;
    private final DailyProductMetricRepository dailyProductMetricRepository;
    private final MeterRegistry meterRegistry;

    @Value("${analytics.etl.enabled:true}")
    private boolean etlEnabled;
    @Value("${analytics.etl.alert.failure-threshold:3}")
    private int failureThreshold;
    @Value("${analytics.etl.alert.stale-days-threshold:2}")
    private int staleDaysThreshold;

    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);
    private final AtomicLong staleDaysGauge = new AtomicLong(0);

    @Scheduled(cron = "${analytics.etl.daily-cron:0 15 1 * * *}")
    @Transactional
    public void runDailyEtl() {
        meterRegistry.gauge("analytics.etl.consecutive_failures", consecutiveFailures);
        meterRegistry.gauge("analytics.etl.stale_days", staleDaysGauge);
        if (!etlEnabled) {
            return;
        }
        LocalDate targetDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        try {
            runEtlForDate(targetDate);
            consecutiveFailures.set(0);
            checkStaleDataWindow();
        } catch (Exception ex) {
            int failures = consecutiveFailures.incrementAndGet();
            meterRegistry.counter("analytics.etl.failures.total").increment();
            if (failures >= Math.max(1, failureThreshold)) {
                log.warn("ALERT analytics.etl repeated failures failures={} threshold={} lastError={}",
                        failures, failureThreshold, ex.getMessage());
            }
            throw ex;
        }
    }

    @Transactional
    public void runEtlForDate(LocalDate targetDate) {
        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();
        long startedAt = System.currentTimeMillis();

        List<ClickstreamEventDocument> events =
                clickstreamEventRepository.findByEventTimeInRange(from, to);
        meterRegistry.counter("analytics.etl.events.processed.total").increment(events.size());

        QualityReport sourceQuality = validateSourceQuality(events, from, to);
        long droppedEvents = sourceQuality.criticalViolations.stream()
                .filter(violation -> violation.startsWith("missing_") || violation.startsWith("out_of_range"))
                .mapToLong(this::extractCountSuffix)
                .sum();
        if (droppedEvents > 0) {
            meterRegistry.counter("analytics.etl.events.dropped.total").increment(droppedEvents);
        }
        if (!sourceQuality.warnings.isEmpty()) {
            log.warn("Analytics ETL source quality warnings targetDate={} warnings={}", targetDate, sourceQuality.warnings);
        }
        if (!sourceQuality.criticalViolations.isEmpty()) {
            throw new IllegalStateException("Analytics ETL source quality failed for " + targetDate
                    + " violations=" + sourceQuality.criticalViolations);
        }

        Map<Long, MutableMetricRow> metricsByProduct = aggregate(events);
        List<DailyProductMetric> rows = toRows(targetDate, metricsByProduct);
        QualityReport martQuality = validateMartQuality(events, rows, targetDate);
        if (!martQuality.warnings.isEmpty()) {
            log.warn("Analytics ETL mart quality warnings targetDate={} warnings={}", targetDate, martQuality.warnings);
        }
        if (!martQuality.criticalViolations.isEmpty()) {
            throw new IllegalStateException("Analytics ETL mart quality failed for " + targetDate
                    + " violations=" + martQuality.criticalViolations);
        }

        int deleted = dailyProductMetricRepository.deleteByMetricDate(targetDate);
        dailyProductMetricRepository.saveAll(rows);
        meterRegistry.counter("analytics.etl.rows.upserted.total").increment(rows.size());

        long durationMs = (System.currentTimeMillis() - startedAt);
        meterRegistry.timer("analytics.etl.duration").record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS);
        log.info(
                "Analytics ETL finished targetDate={} eventsRead={} rowsUpserted={} rowsDeleted={} durationMs={}",
                targetDate,
                events.size(),
                rows.size(),
                deleted,
                durationMs
        );
    }

    private Map<Long, MutableMetricRow> aggregate(List<ClickstreamEventDocument> events) {
        Map<Long, MutableMetricRow> byProduct = new HashMap<>();

        for (ClickstreamEventDocument event : events) {
            String eventType = event.getEventType();
            Long productId = event.getProductId();
            if (eventType == null || productId == null) {
                continue;
            }

            MutableMetricRow row = byProduct.computeIfAbsent(productId, ignored -> new MutableMetricRow());
            if (EVENT_VIEW_PRODUCT.equals(eventType)) {
                row.views++;
                row.uniqueActors.add(actorKey(event));
            } else if (EVENT_ADD_TO_CART.equals(eventType)) {
                row.addToCart++;
                row.uniqueActors.add(actorKey(event));
            } else if (EVENT_PLACE_ORDER.equals(eventType)) {
                row.orders++;
                row.uniqueActors.add(actorKey(event));
            }
        }

        return byProduct;
    }

    private List<DailyProductMetric> toRows(LocalDate targetDate, Map<Long, MutableMetricRow> metricsByProduct) {
        List<DailyProductMetric> rows = new ArrayList<>(metricsByProduct.size());
        for (Map.Entry<Long, MutableMetricRow> entry : metricsByProduct.entrySet()) {
            Long productId = entry.getKey();
            MutableMetricRow row = entry.getValue();
            rows.add(DailyProductMetric.builder()
                    .id(new DailyProductMetricId(targetDate, productId))
                    .views(row.views)
                    .addToCart(row.addToCart)
                    .orders(row.orders)
                    .uniqueUsers(row.uniqueActors.size())
                    .conversionRate(DailyProductMetric.computeConversionRate(row.views, row.orders))
                    .build());
        }
        return rows;
    }

    private String actorKey(ClickstreamEventDocument event) {
        if (event.getUserId() != null) {
            return "u:" + event.getUserId();
        }
        if (event.getGuestId() != null && !event.getGuestId().isBlank()) {
            return "g:" + event.getGuestId();
        }
        return "unknown";
    }

    private static class MutableMetricRow {
        long views;
        long addToCart;
        long orders;
        Set<String> uniqueActors = new HashSet<>();
    }

    private QualityReport validateSourceQuality(List<ClickstreamEventDocument> events, LocalDateTime from, LocalDateTime to) {
        QualityReport report = new QualityReport();
        int missingEventType = 0;
        int missingEventTime = 0;
        int missingProductKey = 0;
        int missingProductKeyPlaceOrder = 0;
        int outOfRangeEventTime = 0;
        int missingActor = 0;
        int unknownEventType = 0;

        for (ClickstreamEventDocument event : events) {
            String eventType = event.getEventType();
            LocalDateTime eventTime = event.getEventTime();
            if (eventType == null || eventType.isBlank()) {
                missingEventType++;
                continue;
            }
            if (eventTime == null) {
                missingEventTime++;
                continue;
            }
            if (eventTime.isBefore(from) || !eventTime.isBefore(to)) {
                outOfRangeEventTime++;
            }

            if (isTrackedEvent(eventType) && event.getUserId() == null
                    && (event.getGuestId() == null || event.getGuestId().isBlank())) {
                missingActor++;
            }

            if (isProductScopedEvent(eventType) && event.getProductId() == null) {
                if (EVENT_PLACE_ORDER.equals(eventType)) {
                    missingProductKeyPlaceOrder++;
                } else {
                    missingProductKey++;
                }
            }

            if (!isTrackedEvent(eventType)) {
                unknownEventType++;
            }
        }

        if (missingEventType > 0) {
            report.criticalViolations.add("missing_event_type=" + missingEventType);
        }
        if (missingEventTime > 0) {
            report.criticalViolations.add("missing_event_time=" + missingEventTime);
        }
        if (missingProductKey > 0) {
            report.criticalViolations.add("missing_product_key=" + missingProductKey);
        }
        if (missingProductKeyPlaceOrder > 0) {
            report.warnings.add("missing_product_key_place_order=" + missingProductKeyPlaceOrder);
        }
        if (outOfRangeEventTime > 0) {
            report.criticalViolations.add("out_of_range_event_time=" + outOfRangeEventTime);
        }
        if (missingActor > 0) {
            report.warnings.add("missing_actor=" + missingActor);
        }
        if (unknownEventType > 0) {
            report.warnings.add("unknown_event_type=" + unknownEventType);
        }
        return report;
    }

    private QualityReport validateMartQuality(List<ClickstreamEventDocument> events, List<DailyProductMetric> rows, LocalDate targetDate) {
        QualityReport report = new QualityReport();

        long trackedProductScopedEvents = events.stream()
                .filter(e -> isProductScopedEvent(e.getEventType()))
                .count();
        if (trackedProductScopedEvents > 0 && rows.isEmpty()) {
            report.criticalViolations.add("missing_date_partition_rows_for_" + targetDate);
        }

        Set<String> duplicateKeys = rows.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getId().getMetricDate() + "|" + r.getId().getProductId(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        if (!duplicateKeys.isEmpty()) {
            report.criticalViolations.add("duplicate_metric_keys=" + duplicateKeys.size());
        }

        long nullMetricKeys = rows.stream()
                .filter(r -> r.getId() == null || r.getId().getMetricDate() == null || r.getId().getProductId() == null)
                .count();
        if (nullMetricKeys > 0) {
            report.criticalViolations.add("null_metric_keys=" + nullMetricKeys);
        }
        return report;
    }

    private boolean isTrackedEvent(String eventType) {
        return EVENT_VIEW_PRODUCT.equals(eventType)
                || EVENT_ADD_TO_CART.equals(eventType)
                || EVENT_PLACE_ORDER.equals(eventType);
    }

    private boolean isProductScopedEvent(String eventType) {
        return EVENT_VIEW_PRODUCT.equals(eventType)
                || EVENT_ADD_TO_CART.equals(eventType)
                || EVENT_PLACE_ORDER.equals(eventType);
    }

    private static class QualityReport {
        private final List<String> criticalViolations = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
    }

    private long extractCountSuffix(String violation) {
        int index = violation.lastIndexOf('=');
        if (index < 0 || index >= violation.length() - 1) {
            return 0;
        }
        try {
            return Long.parseLong(violation.substring(index + 1));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private void checkStaleDataWindow() {
        LocalDate latest = dailyProductMetricRepository.findLatestMetricDate().orElse(null);
        if (latest == null) {
            log.warn("ALERT analytics.etl stale data latestMetricDate missing");
            return;
        }
        long staleDays = java.time.temporal.ChronoUnit.DAYS.between(latest, LocalDate.now(ZoneOffset.UTC));
        staleDaysGauge.set(staleDays);
        if (staleDays > Math.max(0, staleDaysThreshold)) {
            log.warn("ALERT analytics.etl stale data staleDays={} threshold={} latestMetricDate={}",
                    staleDays, staleDaysThreshold, latest);
        }
    }
}
