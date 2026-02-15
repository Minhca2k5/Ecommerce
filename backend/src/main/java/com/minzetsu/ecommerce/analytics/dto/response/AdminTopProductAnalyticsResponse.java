package com.minzetsu.ecommerce.analytics.dto.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTopProductAnalyticsResponse {
    private Long productId;
    private String productName;
    private long views;
    private long addToCart;
    private long orders;
    private long uniqueUsers;
    private BigDecimal conversionRate;
}
