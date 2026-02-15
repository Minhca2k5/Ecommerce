package com.minzetsu.ecommerce.analytics.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DailyProductMetricTest {

    @Test
    void computeConversionRate_shouldReturnZeroWhenViewsIsZero() {
        BigDecimal rate = DailyProductMetric.computeConversionRate(0, 5);
        assertThat(rate).isEqualByComparingTo("0.0000");
    }

    @Test
    void computeConversionRate_shouldReturnScaledRatioWhenViewsPositive() {
        BigDecimal rate = DailyProductMetric.computeConversionRate(8, 3);
        assertThat(rate).isEqualByComparingTo("0.3750");
    }
}
