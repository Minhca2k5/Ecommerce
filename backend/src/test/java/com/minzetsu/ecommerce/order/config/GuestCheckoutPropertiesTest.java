package com.minzetsu.ecommerce.order.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GuestCheckoutPropertiesTest {

    @Test
    void shouldExposeDefaultsAndAllowOverride() {
        GuestCheckoutProperties properties = new GuestCheckoutProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getUsername()).isEqualTo("guest_checkout");
        assertThat(properties.getEmail()).isEqualTo("guest.checkout@local.invalid");
        assertThat(properties.getPassword()).isEqualTo("GuestCheckout@123");
        assertThat(properties.getAccessTokenSecret()).isEqualTo("guest-order-access-secret-change-me");
        assertThat(properties.getAccessTokenTtlMinutes()).isEqualTo(4320L);

        properties.setEnabled(false);
        properties.setUsername("guest_v2");
        properties.setEmail("guest.v2@test.local");
        properties.setPassword("pw");
        properties.setAccessTokenSecret("secret");
        properties.setAccessTokenTtlMinutes(60L);

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getUsername()).isEqualTo("guest_v2");
        assertThat(properties.getEmail()).isEqualTo("guest.v2@test.local");
        assertThat(properties.getPassword()).isEqualTo("pw");
        assertThat(properties.getAccessTokenSecret()).isEqualTo("secret");
        assertThat(properties.getAccessTokenTtlMinutes()).isEqualTo(60L);
    }
}
