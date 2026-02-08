package com.minzetsu.ecommerce.order.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CheckoutPricingPropertiesTest {

    private final CheckoutPricingProperties properties = new CheckoutPricingProperties();

    @Test
    void normalizeCurrency_shouldReturnDefaultWhenInputIsNull() {
        properties.setDefaultCurrency("VND");

        String result = properties.normalizeCurrency(null);

        assertThat(result).isEqualTo("VND");
    }

    @Test
    void normalizeCurrency_shouldReturnDefaultWhenInputIsBlank() {
        properties.setDefaultCurrency("USD");

        String result = properties.normalizeCurrency("   ");

        assertThat(result).isEqualTo("USD");
    }

    @Test
    void normalizeCurrency_shouldTrimAndUppercaseInput() {
        String result = properties.normalizeCurrency("  jPy ");

        assertThat(result).isEqualTo("JPY");
    }
}
