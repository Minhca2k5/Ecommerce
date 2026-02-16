package com.minzetsu.ecommerce.analytics.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsRealtimeCounterService {

    private static final String EVENT_VIEW_PRODUCT = "VIEW_PRODUCT";
    private static final String EVENT_ADD_TO_CART = "ADD_TO_CART";
    private static final String EVENT_PLACE_ORDER = "PLACE_ORDER";
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.BASIC_ISO_DATE;

    private final StringRedisTemplate redisTemplate;

    @Value("${analytics.realtime.enabled:true}")
    private boolean realtimeEnabled;

    @Value("${analytics.realtime.ttl-hours:72}")
    private long realtimeTtlHours;

    public void incrementEvent(String eventType, Long productId, Long userId, String guestId, LocalDateTime eventTimeUtc) {
        if (!realtimeEnabled || eventType == null || productId == null || eventTimeUtc == null) {
            return;
        }
        String metricField = metricField(eventType);
        if (metricField == null) {
            return;
        }

        LocalDate metricDate = eventTimeUtc.toLocalDate();
        String dateKey = datePart(metricDate);
        String funnelKey = funnelKey(dateKey);
        String productsKey = productsKey(dateKey);
        String productKey = productKey(dateKey, productId);
        String actorsKey = actorsKey(dateKey, productId);

        redisTemplate.opsForHash().increment(funnelKey, metricField, 1L);
        redisTemplate.opsForSet().add(productsKey, String.valueOf(productId));
        redisTemplate.opsForHash().increment(productKey, metricField, 1L);
        String actorKey = actorKey(userId, guestId);
        if (actorKey != null) {
            redisTemplate.opsForSet().add(actorsKey, actorKey);
        }
        long ttlSeconds = Math.max(1L, realtimeTtlHours * 3600L);
        redisTemplate.expire(funnelKey, java.time.Duration.ofSeconds(ttlSeconds));
        redisTemplate.expire(productsKey, java.time.Duration.ofSeconds(ttlSeconds));
        redisTemplate.expire(productKey, java.time.Duration.ofSeconds(ttlSeconds));
        redisTemplate.expire(actorsKey, java.time.Duration.ofSeconds(ttlSeconds));
    }

    public RealtimeFunnel readFunnel(LocalDate metricDate) {
        if (!realtimeEnabled || metricDate == null) {
            return RealtimeFunnel.empty();
        }
        Map<Object, Object> fields = redisTemplate.opsForHash().entries(funnelKey(datePart(metricDate)));
        return new RealtimeFunnel(
                parseLong(fields.get("views")),
                parseLong(fields.get("addToCart")),
                parseLong(fields.get("orders"))
        );
    }

    public Map<Long, RealtimeProductMetric> readTopProducts(LocalDate metricDate) {
        if (!realtimeEnabled || metricDate == null) {
            return Collections.emptyMap();
        }
        Set<String> productIds = redisTemplate.opsForSet().members(productsKey(datePart(metricDate)));
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        String dateKey = datePart(metricDate);
        Map<Long, RealtimeProductMetric> result = new HashMap<>();
        for (String productIdRaw : productIds) {
            Long productId = parseProductId(productIdRaw);
            if (productId == null) {
                continue;
            }
            Map<Object, Object> fields = redisTemplate.opsForHash().entries(productKey(dateKey, productId));
            Long uniqueUsers = redisTemplate.opsForSet().size(actorsKey(dateKey, productId));
            result.put(productId, new RealtimeProductMetric(
                    parseLong(fields.get("views")),
                    parseLong(fields.get("addToCart")),
                    parseLong(fields.get("orders")),
                    uniqueUsers == null ? 0L : uniqueUsers
            ));
        }
        return result;
    }

    private String metricField(String eventType) {
        if (EVENT_VIEW_PRODUCT.equals(eventType)) {
            return "views";
        }
        if (EVENT_ADD_TO_CART.equals(eventType)) {
            return "addToCart";
        }
        if (EVENT_PLACE_ORDER.equals(eventType)) {
            return "orders";
        }
        return null;
    }

    private String actorKey(Long userId, String guestId) {
        if (userId != null) {
            return "u:" + userId;
        }
        if (guestId != null && !guestId.isBlank()) {
            return "g:" + guestId;
        }
        return null;
    }

    private String datePart(LocalDate date) {
        return DAY_FMT.format(date);
    }

    private String funnelKey(String datePart) {
        return "analytics:realtime:" + datePart + ":funnel";
    }

    private String productsKey(String datePart) {
        return "analytics:realtime:" + datePart + ":products";
    }

    private String productKey(String datePart, Long productId) {
        return "analytics:realtime:" + datePart + ":product:" + productId;
    }

    private String actorsKey(String datePart, Long productId) {
        return "analytics:realtime:" + datePart + ":product:" + productId + ":actors";
    }

    private long parseLong(Object value) {
        if (value == null) {
            return 0L;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException ignored) {
            return 0L;
        }
    }

    private Long parseProductId(String raw) {
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public record RealtimeFunnel(long views, long addToCart, long orders) {
        public static RealtimeFunnel empty() {
            return new RealtimeFunnel(0L, 0L, 0L);
        }
    }

    public record RealtimeProductMetric(long views, long addToCart, long orders, long uniqueUsers) {}
}
