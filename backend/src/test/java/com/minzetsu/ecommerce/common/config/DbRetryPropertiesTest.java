package com.minzetsu.ecommerce.common.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DbRetryPropertiesTest {

    @Test
    void shouldExposeDefaultsAndAllowOverride() {
        DbRetryProperties properties = new DbRetryProperties();

        assertThat(properties.getMaxAttempts()).isEqualTo(3);
        assertThat(properties.getBackoffMs()).isEqualTo(80L);
        assertThat(properties.getBackoffMultiplier()).isEqualTo(2.0);
        assertThat(properties.getMaxBackoffMs()).isEqualTo(500L);

        properties.setMaxAttempts(7);
        properties.setBackoffMs(120L);
        properties.setBackoffMultiplier(1.5);
        properties.setMaxBackoffMs(900L);

        assertThat(properties.getMaxAttempts()).isEqualTo(7);
        assertThat(properties.getBackoffMs()).isEqualTo(120L);
        assertThat(properties.getBackoffMultiplier()).isEqualTo(1.5);
        assertThat(properties.getMaxBackoffMs()).isEqualTo(900L);
    }
}
