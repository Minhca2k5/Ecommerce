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
    private long paymentSuccess;
    private BigDecimal viewToCartRate;
    private BigDecimal cartToOrderRate;
    private BigDecimal orderToPaymentRate;
    private BigDecimal viewToOrderRate;
    private LocalDate previousFrom;
    private LocalDate previousTo;
    private long previousViews;
    private long previousAddToCart;
    private long previousOrders;
    private long previousPaymentSuccess;
    private BigDecimal previousViewToOrderRate;
    private BigDecimal viewsChangeRate;
    private BigDecimal addToCartChangeRate;
    private BigDecimal ordersChangeRate;
    private BigDecimal viewToOrderRateChange;
    private long todayViews;
    private long todayAddToCart;
    private long todayOrders;
    private long todayPaymentSuccess;
}
