package com.minzetsu.ecommerce.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class RequestIdFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String MDC_REQUEST_ID_KEY = "requestId";
    private static final int MAX_REQUEST_ID_LENGTH = 64;
    private static final Pattern ALLOWED_REQUEST_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9._:-]{1,64}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = sanitizeRequestId(request.getHeader(REQUEST_ID_HEADER));
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_REQUEST_ID_KEY, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_REQUEST_ID_KEY);
        }
    }

    private String sanitizeRequestId(String rawRequestId) {
        if (rawRequestId == null) {
            return null;
        }
        String requestId = rawRequestId.trim();
        if (requestId.isBlank() || requestId.length() > MAX_REQUEST_ID_LENGTH) {
            return null;
        }
        if (!ALLOWED_REQUEST_ID_PATTERN.matcher(requestId).matches()) {
            return null;
        }
        return requestId;
    }
}
