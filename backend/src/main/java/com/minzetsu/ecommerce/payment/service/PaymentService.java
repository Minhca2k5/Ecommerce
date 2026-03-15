package com.minzetsu.ecommerce.payment.service;

import com.minzetsu.ecommerce.payment.dto.filter.PaymentFilter;
import com.minzetsu.ecommerce.payment.dto.request.PaymentRequest;
import com.minzetsu.ecommerce.payment.dto.response.PaymentResponse;
import com.minzetsu.ecommerce.payment.entity.Payment;
import com.minzetsu.ecommerce.payment.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {
    void updatePaymentStatusById(PaymentStatus status, Long id);
    boolean existsById(Long id);
    Payment getPaymentById(Long id);
    Payment getPaymentByIdAndUserId(Long id, Long userId);
    Payment getPaymentByProviderTxnId(String providerTxnId);
    Page<PaymentResponse> searchPaymentResponses(PaymentFilter filter, Pageable pageable);
    List<PaymentResponse> getPaymentResponsesByOrderId(Long orderId, Long userId);
    PaymentResponse getPaymentResponseById(Long id, Long userId);
    PaymentResponse createPaymentResponse(PaymentRequest request, Long userId, Long orderId, String idempotencyKey);
}

