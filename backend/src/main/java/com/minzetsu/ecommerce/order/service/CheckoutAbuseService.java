package com.minzetsu.ecommerce.order.service;

public interface CheckoutAbuseService {
    void assertAllowed(String scope);

    void recordFailure(String scope);

    void recordSuccess(String scope);
}
