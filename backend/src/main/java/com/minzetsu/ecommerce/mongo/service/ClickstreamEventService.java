package com.minzetsu.ecommerce.mongo.service;

import com.minzetsu.ecommerce.analytics.service.AnalyticsRealtimeCounterService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import com.minzetsu.ecommerce.mongo.document.ClickstreamEventDocument;
import com.minzetsu.ecommerce.mongo.repository.ClickstreamEventRepository;

@Service
@RequiredArgsConstructor
public class ClickstreamEventService {

    private static final Logger logger = LoggerFactory.getLogger(ClickstreamEventService.class);
    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_USER_AGENT = "User-Agent";
    private static final String HEADER_CLIENT_SOURCE = "X-Client-Source";
    private static final String EVENT_VIEW_PRODUCT = "VIEW_PRODUCT";
    private static final String EVENT_ADD_TO_CART = "ADD_TO_CART";
    private static final String EVENT_PLACE_ORDER = "PLACE_ORDER";
    private static final String EVENT_PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
    private static final String EVENT_SEARCH = "SEARCH";
    private static final String EVENT_CLICK_BANNER = "CLICK_BANNER";
    private static final String SCHEMA_VERSION = "v1";
    private static final String DEFAULT_SOURCE = "WEB";
    private static final String UNKNOWN_EVENT = "UNKNOWN";

    private final ClickstreamEventRepository repository;
    private final MeterRegistry meterRegistry;
    private final AnalyticsRealtimeCounterService analyticsRealtimeCounterService;

    public void recordProductView(Long userId, Long productId) {
        recordProductView(userId, null, productId);
    }

    public void recordProductView(Long userId, String guestId, Long productId) {
        ClickstreamEventDocument doc = buildBaseEvent(EVENT_VIEW_PRODUCT, userId, guestId);
        doc.setProductId(productId);
        persistIfValid(doc);
    }

    public void recordAddToCart(Long userId, String guestId, Long productId) {
        ClickstreamEventDocument doc = buildBaseEvent(EVENT_ADD_TO_CART, userId, guestId);
        doc.setProductId(productId);
        persistIfValid(doc);
    }

    public void recordPlaceOrder(Long userId, String guestId, Long productId) {
        ClickstreamEventDocument doc = buildBaseEvent(EVENT_PLACE_ORDER, userId, guestId);
        doc.setProductId(productId);
        persistIfValid(doc);
    }

    public void recordPaymentSuccess(Long userId) {
        persistIfValid(buildBaseEvent(EVENT_PAYMENT_SUCCESS, userId, null));
    }

    public void recordSearch(Long userId, String keyword) {
        ClickstreamEventDocument doc = buildBaseEvent(EVENT_SEARCH, userId, null);
        doc.setKeyword(keyword);
        persistIfValid(doc);
    }

    public void recordBannerClick(Long userId, String guestId, String bannerKey, String placement, String targetPath) {
        ClickstreamEventDocument doc = buildBaseEvent(EVENT_CLICK_BANNER, userId, guestId);
        doc.setKeyword(buildBannerKeyword(bannerKey, placement, targetPath));
        persistIfValid(doc);
    }

    private String buildBannerKeyword(String bannerKey, String placement, String targetPath) {
        String key = bannerKey == null ? "" : bannerKey.trim();
        String zone = placement == null ? "" : placement.trim();
        String target = targetPath == null ? "" : targetPath.trim();
        return "banner=" + key + "|placement=" + zone + "|target=" + target;
    }

    private ClickstreamEventDocument buildBaseEvent(String eventType, Long userId, String guestId) {
        ClickstreamEventDocument doc = new ClickstreamEventDocument();
        doc.setEventType(eventType);
        doc.setUserId(userId);
        doc.setGuestId(guestId);
        fillRequestContext(doc);
        return doc;
    }

    private void fillRequestContext(ClickstreamEventDocument doc) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        doc.setEventTime(now);
        doc.setCreatedAt(now);
        String requestId = MDC.get("requestId");
        doc.setRequestId((requestId == null || requestId.isBlank()) ? UUID.randomUUID().toString() : requestId);
        doc.setSchemaVersion(SCHEMA_VERSION);
        doc.setSource(DEFAULT_SOURCE);
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return;
        }
        doc.setUserAgent(request.getHeader(HEADER_USER_AGENT));
        doc.setIp(resolveClientIp(request));
        String source = request.getHeader(HEADER_CLIENT_SOURCE);
        if (source != null && !source.isBlank()) {
            doc.setSource(source.trim());
        }
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader(HEADER_X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            int commaIndex = forwarded.indexOf(',');
            return commaIndex > 0 ? forwarded.substring(0, commaIndex).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }

    private void persistIfValid(ClickstreamEventDocument doc) {
        String reason = validate(doc);
        if (reason != null) {
            String eventType = doc.getEventType() == null ? UNKNOWN_EVENT : doc.getEventType();
            meterRegistry.counter("clickstream.events.dropped", "eventType", eventType, "reason", reason).increment();
            logger.warn("Dropped clickstream event type={} reason={} requestId={}",
                    eventType, reason, doc.getRequestId());
            return;
        }
        safeSave(doc);
    }

    private String validate(ClickstreamEventDocument doc) {
        if (doc.getEventType() == null || doc.getEventType().isBlank()) {
            return "missing_event_type";
        }
        if (doc.getEventTime() == null) {
            return "missing_event_time";
        }
        if (doc.getRequestId() == null || doc.getRequestId().isBlank()) {
            return "missing_request_id";
        }
        if (doc.getSource() == null || doc.getSource().isBlank()) {
            return "missing_source";
        }
        if (doc.getUserId() == null && (doc.getGuestId() == null || doc.getGuestId().isBlank())) {
            return "missing_actor";
        }
        if ((EVENT_VIEW_PRODUCT.equals(doc.getEventType())
                || EVENT_ADD_TO_CART.equals(doc.getEventType())
                || EVENT_PLACE_ORDER.equals(doc.getEventType()))
                && doc.getProductId() == null) {
            return "missing_product_id";
        }
        return null;
    }

    private void safeSave(ClickstreamEventDocument doc) {
        try {
            repository.save(doc);
            meterRegistry.counter("clickstream.events.saved", "eventType", doc.getEventType()).increment();
            analyticsRealtimeCounterService.incrementEvent(
                    doc.getEventType(),
                    doc.getProductId(),
                    doc.getUserId(),
                    doc.getGuestId(),
                    doc.getEventTime());
        } catch (Exception ex) {
            meterRegistry.counter("clickstream.events.failed", "eventType", doc.getEventType()).increment();
            logger.warn("Failed to persist clickstream event type={} requestId={} reason={}",
                    doc.getEventType(),
                    doc.getRequestId(),
                    ex.getMessage());
        }
    }
}
