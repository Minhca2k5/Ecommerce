package com.minzetsu.ecommerce.analytics.service;

import com.minzetsu.ecommerce.analytics.entity.DailyProductMetric;
import com.minzetsu.ecommerce.analytics.entity.DailyProductMetricId;
import com.minzetsu.ecommerce.analytics.repository.DailyProductMetricRepository;
import com.minzetsu.ecommerce.mongo.ClickstreamEventDocument;
import com.minzetsu.ecommerce.mongo.ClickstreamEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Value("${analytics.etl.enabled:true}")
    private boolean etlEnabled;

    @Scheduled(cron = "${analytics.etl.daily-cron:0 15 1 * * *}")
    @Transactional
    public void runDailyEtl() {
        if (!etlEnabled) {
            return;
        }
        LocalDate targetDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        runEtlForDate(targetDate);
    }

    @Transactional
    public void runEtlForDate(LocalDate targetDate) {
        LocalDateTime from = targetDate.atStartOfDay();
        LocalDateTime to = targetDate.plusDays(1).atStartOfDay();
        long startedAt = System.currentTimeMillis();

        List<ClickstreamEventDocument> events =
                clickstreamEventRepository.findByEventTimeGreaterThanEqualAndEventTimeLessThan(from, to);
        Map<Long, MutableMetricRow> metricsByProduct = aggregate(events);
        List<DailyProductMetric> rows = toRows(targetDate, metricsByProduct);

        int deleted = dailyProductMetricRepository.deleteByMetricDate(targetDate);
        dailyProductMetricRepository.saveAll(rows);

        log.info(
                "Analytics ETL finished targetDate={} eventsRead={} rowsUpserted={} rowsDeleted={} durationMs={}",
                targetDate,
                events.size(),
                rows.size(),
                deleted,
                (System.currentTimeMillis() - startedAt)
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
}
