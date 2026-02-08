package com.minzetsu.ecommerce.common.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OutboundHttpPropertiesTest {

    @Test
    void shouldExposeDefaultsAndAllowOverride() {
        OutboundHttpProperties properties = new OutboundHttpProperties();

        assertThat(properties.getConnectTimeoutMs()).isEqualTo(2000);
        assertThat(properties.getReadTimeoutMs()).isEqualTo(3000);
        assertThat(properties.getMaxAttempts()).isEqualTo(3);
        assertThat(properties.getBackoffMs()).isEqualTo(200L);
        assertThat(properties.getBackoffMultiplier()).isEqualTo(2.0);
        assertThat(properties.getMaxBackoffMs()).isEqualTo(2000L);

        properties.setConnectTimeoutMs(1000);
        properties.setReadTimeoutMs(1200);
        properties.setMaxAttempts(5);
        properties.setBackoffMs(100L);
        properties.setBackoffMultiplier(1.3);
        properties.setMaxBackoffMs(800L);

        assertThat(properties.getConnectTimeoutMs()).isEqualTo(1000);
        assertThat(properties.getReadTimeoutMs()).isEqualTo(1200);
        assertThat(properties.getMaxAttempts()).isEqualTo(5);
        assertThat(properties.getBackoffMs()).isEqualTo(100L);
        assertThat(properties.getBackoffMultiplier()).isEqualTo(1.3);
        assertThat(properties.getMaxBackoffMs()).isEqualTo(800L);
    }
}
