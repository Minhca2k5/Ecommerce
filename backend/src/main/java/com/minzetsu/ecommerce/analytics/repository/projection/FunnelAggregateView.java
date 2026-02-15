package com.minzetsu.ecommerce.analytics.repository.projection;

public interface FunnelAggregateView {
    Long getViews();
    Long getAddToCart();
    Long getOrders();
}
