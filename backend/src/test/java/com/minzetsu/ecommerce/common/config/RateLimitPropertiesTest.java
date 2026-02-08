package com.minzetsu.ecommerce.common.config;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitPropertiesTest {

    @Test
    void shouldExposeDefaultsAndAllowOverride() {
        RateLimitProperties properties = new RateLimitProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getPublicRule().getCapacity()).isEqualTo(60);
        assertThat(properties.getAuthRule().getCapacity()).isEqualTo(10);

        properties.setAllowK6UserAgentBypass(true);
        properties.setBucketTtl(Duration.ofMinutes(2));
        properties.setCleanupInterval(Duration.ofSeconds(30));
        properties.setBypass(List.of("/health"));

        RateLimitProperties.Rule rule = new RateLimitProperties.Rule(5, 2, Duration.ofSeconds(15));
        properties.setUserRule(rule);

        assertThat(properties.isAllowK6UserAgentBypass()).isTrue();
        assertThat(properties.getBucketTtl()).isEqualTo(Duration.ofMinutes(2));
        assertThat(properties.getCleanupInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getBypass()).containsExactly("/health");
        assertThat(properties.getUserRule().getCapacity()).isEqualTo(5);
        assertThat(properties.getUserRule().getRefillTokens()).isEqualTo(2);
        assertThat(properties.getUserRule().getPeriod()).isEqualTo(Duration.ofSeconds(15));
    }
}
