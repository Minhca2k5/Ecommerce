package com.minzetsu.ecommerce.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RateLimitFilter extends OncePerRequestFilter {
    private static final String HEADER_FORWARDED_FOR = "X-Forwarded-For";
    private static final String HEADER_USER_AGENT = "User-Agent";
    private final RateLimitProperties properties;
    private final Counter blockedCounter;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong lastCleanupNanos = new AtomicLong(System.nanoTime());

    public RateLimitFilter(RateLimitProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.blockedCounter = meterRegistry.counter("rate_limit_blocked_total");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();
        if (isBypassed(path)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (properties.isAllowK6UserAgentBypass() && isK6UserAgent(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ruleKey = resolveRuleKey(path);
        RateLimitProperties.Rule rule = resolveRule(ruleKey);
        String bucketKey = resolveBucketKey(ruleKey, request);
        maybeCleanupBuckets(properties.getCleanupInterval(), properties.getBucketTtl());
        TokenBucket bucket = buckets.computeIfAbsent(
                bucketKey,
                key -> new TokenBucket(rule.getCapacity(), rule.getRefillTokens(), rule.getPeriod())
        );

        if (!bucket.tryConsume()) {
            blockedCounter.increment();
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"message\":\"Too many requests\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isBypassed(String path) {
        for (String pattern : properties.getBypass()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String resolveRuleKey(String path) {
        if (pathMatcher.match("/api/auth/**", path)) {
            return "auth";
        }
        if (pathMatcher.match("/api/public/**", path)) {
            return "public";
        }
        if (pathMatcher.match("/api/admin/**", path)) {
            return "admin";
        }
        return "user";
    }

    private RateLimitProperties.Rule resolveRule(String ruleKey) {
        return switch (ruleKey) {
            case "auth" -> properties.getAuthRule();
            case "public" -> properties.getPublicRule();
            case "admin" -> properties.getAdminRule();
            default -> properties.getUserRule();
        };
    }

    private boolean isK6UserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader(HEADER_USER_AGENT);
        return userAgent != null && userAgent.toLowerCase().contains("k6");
    }

    private String resolveBucketKey(String ruleKey, HttpServletRequest request) {
        String clientKey = resolveClientKey(request);
        return ruleKey + ":" + clientKey;
    }

    private String resolveClientKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() != null) {
            return String.valueOf(authentication.getName());
        }
        String forwarded = request.getHeader(HEADER_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void maybeCleanupBuckets(Duration cleanupInterval, Duration bucketTtl) {
        long now = System.nanoTime();
        long last = lastCleanupNanos.get();
        if (now - last < cleanupInterval.toNanos()) {
            return;
        }
        if (!lastCleanupNanos.compareAndSet(last, now)) {
            return;
        }
        long ttlNanos = bucketTtl.toNanos();
        buckets.entrySet().removeIf(entry -> now - entry.getValue().getLastAccessNanos() > ttlNanos);
    }

    private static class TokenBucket {
        private final int capacity;
        private final int refillTokens;
        private final long refillPeriodNanos;
        private long tokens;
        private long lastRefillNanos;
        private long lastAccessNanos;

        private TokenBucket(int capacity, int refillTokens, Duration period) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.refillPeriodNanos = period.toNanos();
            this.tokens = capacity;
            this.lastRefillNanos = System.nanoTime();
            this.lastAccessNanos = this.lastRefillNanos;
        }

        private synchronized boolean tryConsume() {
            lastAccessNanos = System.nanoTime();
            refill();
            if (tokens <= 0) {
                return false;
            }
            tokens -= 1;
            return true;
        }

        private void refill() {
            long now = System.nanoTime();
            long elapsed = now - lastRefillNanos;
            if (elapsed < refillPeriodNanos) {
                return;
            }
            long periods = elapsed / refillPeriodNanos;
            long refill = periods * refillTokens;
            tokens = Math.min(capacity, tokens + refill);
            lastRefillNanos = lastRefillNanos + (periods * refillPeriodNanos);
        }

        private long getLastAccessNanos() {
            return lastAccessNanos;
        }
    }
}
