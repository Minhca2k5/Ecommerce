package com.minzetsu.ecommerce.order.event;

public class OrderCreatedEvent {
    private final Long orderId;
    private final Long userId;

    public OrderCreatedEvent(Long orderId, Long userId) {
        this.orderId = orderId;
        this.userId = userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getUserId() {
        return userId;
    }
}
