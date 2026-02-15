package com.minzetsu.ecommerce.analytics.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminFunnelAnalyticsResponse {
    private LocalDate from;
    private LocalDate to;
    private long views;
    private long addToCart;
    private long orders;
    private BigDecimal viewToCartRate;
    private BigDecimal cartToOrderRate;
    private BigDecimal viewToOrderRate;
}
