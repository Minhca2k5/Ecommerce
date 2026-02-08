package com.minzetsu.ecommerce.common.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditTelemetryPropertiesTest {

    @Test
    void shouldExposeDefaultsAndAllowOverride() {
        AuditTelemetryProperties properties = new AuditTelemetryProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getChannel()).isEqualTo("audit-log-events");

        properties.setEnabled(false);
        properties.setChannel("audit-custom");

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getChannel()).isEqualTo("audit-custom");
    }
}
