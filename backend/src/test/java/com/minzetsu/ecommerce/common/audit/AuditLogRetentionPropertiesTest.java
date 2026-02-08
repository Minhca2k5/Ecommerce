package com.minzetsu.ecommerce.common.audit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuditLogRetentionPropertiesTest {

    @Test
    void shouldExposeDefaultsAndAllowOverride() {
        AuditLogRetentionProperties properties = new AuditLogRetentionProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getRetentionDays()).isEqualTo(180);
        assertThat(properties.getCleanupIntervalMs()).isEqualTo(86400000L);
        assertThat(properties.getBatchSize()).isEqualTo(1000);

        properties.setEnabled(false);
        properties.setRetentionDays(90);
        properties.setCleanupIntervalMs(60000L);
        properties.setBatchSize(250);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getRetentionDays()).isEqualTo(90);
        assertThat(properties.getCleanupIntervalMs()).isEqualTo(60000L);
        assertThat(properties.getBatchSize()).isEqualTo(250);
    }
}
