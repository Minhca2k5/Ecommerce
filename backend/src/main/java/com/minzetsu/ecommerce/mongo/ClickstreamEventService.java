package com.minzetsu.ecommerce.mongo;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class ClickstreamEventService {

    private static final String HEADER_X_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_USER_AGENT = "User-Agent";

    private final ClickstreamEventRepository repository;

    public void recordProductView(Long userId, Long productId) {
        ClickstreamEventDocument doc = new ClickstreamEventDocument();
        doc.setEventType("VIEW");
        doc.setUserId(userId);
        doc.setProductId(productId);
        fillRequestContext(doc);
        safeSave(doc);
    }

    public void recordSearch(Long userId, String keyword) {
        ClickstreamEventDocument doc = new ClickstreamEventDocument();
        doc.setEventType("SEARCH");
        doc.setUserId(userId);
        doc.setKeyword(keyword);
        fillRequestContext(doc);
        safeSave(doc);
    }

    private void fillRequestContext(ClickstreamEventDocument doc) {
        doc.setCreatedAt(LocalDateTime.now());
        doc.setRequestId(MDC.get("requestId"));
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return;
        }
        doc.setUserAgent(request.getHeader(HEADER_USER_AGENT));
        doc.setIp(resolveClientIp(request));
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

    private void safeSave(ClickstreamEventDocument doc) {
        try {
            repository.save(doc);
        } catch (Exception ignored) {
        }
    }
}
