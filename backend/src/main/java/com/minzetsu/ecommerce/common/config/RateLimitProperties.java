package com.minzetsu.ecommerce.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
    private boolean enabled = true;
    private Rule publicRule = new Rule(60, 60, Duration.ofMinutes(1));
    private Rule authRule = new Rule(10, 10, Duration.ofMinutes(1));
    private Rule userRule = new Rule(120, 120, Duration.ofMinutes(1));
    private Rule adminRule = new Rule(60, 60, Duration.ofMinutes(1));
    private boolean allowK6UserAgentBypass = false;
    private Duration bucketTtl = Duration.ofMinutes(10);
    private Duration cleanupInterval = Duration.ofMinutes(5);
    private List<String> bypass = List.of(
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/docs/**"
    );

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Rule getPublicRule() {
        return publicRule;
    }

    public void setPublicRule(Rule publicRule) {
        this.publicRule = publicRule;
    }

    public Rule getAuthRule() {
        return authRule;
    }

    public void setAuthRule(Rule authRule) {
        this.authRule = authRule;
    }

    public Rule getUserRule() {
        return userRule;
    }

    public void setUserRule(Rule userRule) {
        this.userRule = userRule;
    }

    public Rule getAdminRule() {
        return adminRule;
    }

    public void setAdminRule(Rule adminRule) {
        this.adminRule = adminRule;
    }

    public List<String> getBypass() {
        return bypass;
    }

    public void setBypass(List<String> bypass) {
        this.bypass = bypass;
    }

    public boolean isAllowK6UserAgentBypass() {
        return allowK6UserAgentBypass;
    }

    public void setAllowK6UserAgentBypass(boolean allowK6UserAgentBypass) {
        this.allowK6UserAgentBypass = allowK6UserAgentBypass;
    }

    public Duration getBucketTtl() {
        return bucketTtl;
    }

    public void setBucketTtl(Duration bucketTtl) {
        this.bucketTtl = bucketTtl;
    }

    public Duration getCleanupInterval() {
        return cleanupInterval;
    }

    public void setCleanupInterval(Duration cleanupInterval) {
        this.cleanupInterval = cleanupInterval;
    }

    public static class Rule {
        private int capacity;
        private int refillTokens;
        private Duration period;

        public Rule() {
        }

        public Rule(int capacity, int refillTokens, Duration period) {
            this.capacity = capacity;
            this.refillTokens = refillTokens;
            this.period = period;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getRefillTokens() {
            return refillTokens;
        }

        public void setRefillTokens(int refillTokens) {
            this.refillTokens = refillTokens;
        }

        public Duration getPeriod() {
            return period;
        }

        public void setPeriod(Duration period) {
            this.period = period;
        }
    }
}
