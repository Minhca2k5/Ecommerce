package com.minzetsu.ecommerce.messaging;

public enum DomainEventType {
    ORDER_CREATED,
    ORDER_STATUS_UPDATED,
    PAYMENT_CREATED,
    PAYMENT_SUCCEEDED,
    INVENTORY_LOW,
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED
}
