package com.minzetsu.ecommerce.common.config;

import com.minzetsu.ecommerce.common.config.RequestIdFilter;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTimeMs = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long latencyMs = System.currentTimeMillis() - startTimeMs;
            String requestId = MDC.get(RequestIdFilter.MDC_REQUEST_ID_KEY);
            String userId = resolveUserId();
            String method = request.getMethod();
            String path = request.getRequestURI();
            int status = response.getStatus();

            logger.info(
                    "{\"requestId\":\"{}\",\"userId\":\"{}\",\"method\":\"{}\",\"path\":\"{}\",\"status\":{},\"latencyMs\":{}}",
                    safe(requestId),
                    safe(userId),
                    safe(method),
                    safe(path),
                    status,
                    latencyMs
            );
        }
    }

    private String resolveUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return String.valueOf(userDetails.getId());
        }
        return "";
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\\\"");
    }
}
