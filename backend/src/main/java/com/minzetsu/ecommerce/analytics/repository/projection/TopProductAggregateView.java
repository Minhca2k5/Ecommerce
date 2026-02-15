package com.minzetsu.ecommerce.analytics.repository.projection;

public interface TopProductAggregateView {
    Long getProductId();
    String getProductName();
    Long getViews();
    Long getAddToCart();
    Long getOrders();
    Long getUniqueUsers();
}
