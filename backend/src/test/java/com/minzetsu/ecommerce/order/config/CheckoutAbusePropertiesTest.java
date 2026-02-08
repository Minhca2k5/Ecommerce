package com.minzetsu.ecommerce.order.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutAbusePropertiesTest {

    @Test
    void shouldExposeDefaultsAndAllowOverride() {
        CheckoutAbuseProperties properties = new CheckoutAbuseProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getMaxFailures()).isEqualTo(5);
        assertThat(properties.getWindowMinutes()).isEqualTo(10);

        properties.setEnabled(false);
        properties.setMaxFailures(9);
        properties.setWindowMinutes(30);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getMaxFailures()).isEqualTo(9);
        assertThat(properties.getWindowMinutes()).isEqualTo(30);
    }
}
