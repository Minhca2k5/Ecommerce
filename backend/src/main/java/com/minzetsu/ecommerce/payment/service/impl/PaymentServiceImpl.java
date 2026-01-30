package com.minzetsu.ecommerce.payment.service.impl;

import com.minzetsu.ecommerce.common.exception.AlreadyExistException;
import com.minzetsu.ecommerce.common.audit.AuditAction;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.service.OrderService;
import com.minzetsu.ecommerce.payment.dto.filter.PaymentFilter;
import com.minzetsu.ecommerce.payment.dto.request.PaymentRequest;
import com.minzetsu.ecommerce.payment.dto.response.PaymentResponse;
import com.minzetsu.ecommerce.payment.entity.Payment;
import com.minzetsu.ecommerce.payment.entity.PaymentStatus;
import com.minzetsu.ecommerce.payment.mapper.PaymentMapper;
import com.minzetsu.ecommerce.payment.repository.PaymentRepository;
import com.minzetsu.ecommerce.payment.repository.PaymentSpecification;
import com.minzetsu.ecommerce.payment.service.PaymentService;
import com.minzetsu.ecommerce.promotion.service.VoucherUseService;
import com.minzetsu.ecommerce.notification.event.WebhookEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService;
    private final VoucherUseService voucherUseService;
    private final ApplicationEventPublisher eventPublisher;

    private Payment getExistingPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));
    }

    @Override
    @Transactional
    @AuditAction(action = "PAYMENT_STATUS_UPDATED", entityType = "PAYMENT", idParamIndex = 1)
    public void updatePaymentStatusById(PaymentStatus status, Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Payment not found with id: " + id);
        }
        paymentRepository.updateByStatusAndId(status, id);
        eventPublisher.publishEvent(new WebhookEvent(
                "PAYMENT_STATUS_UPDATED",
                "PAYMENT",
                id,
                null
        ));
    }

    @Override
    public boolean existsById(Long id) {
        return paymentRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentById(Long id) {
        return getExistingPayment(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPaymentByIdAndUserId(Long id, Long userId) {
        Payment payment = getExistingPayment(id);
        if (!payment.getOrder().getUser().getId().equals(userId)) {
            throw new UnAuthorizedException("User not authorized to access this payment");
        }
        return payment;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> searchPaymentResponses(PaymentFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                paymentRepository,
                PaymentSpecification.filter(filter),
                paymentMapper::toResponse
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentResponsesByOrderId(Long orderId, Long userId) {
        if (userId != null) {
            orderService.getOrderByIdAndUserId(orderId, userId); // xác thực quyền truy cập
        }
        List<Payment> payments = paymentRepository.findByOrderIdOrderByUpdatedAtDesc(orderId);
        return paymentMapper.toResponseList(payments);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentResponseById(Long id, Long userId) {
        Payment payment = (userId != null)
                ? getPaymentByIdAndUserId(id, userId)
                : getPaymentById(id);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    @AuditAction(action = "PAYMENT_CREATED", entityType = "PAYMENT")
    public PaymentResponse createPaymentResponse(PaymentRequest request, Long userId, Long orderId) {
        Order order = orderService.getOrderByIdAndUserId(orderId, userId);
        List<Payment> existingPayments = paymentRepository.findByOrderIdOrderByUpdatedAtDesc(orderId);
        if (!existingPayments.isEmpty() && existingPayments.stream().anyMatch(p -> (p.getStatus() == PaymentStatus.INITIATED || p.getStatus() == PaymentStatus.SUCCEEDED))) {
            throw new AlreadyExistException("Payment already exists for order with id: " + orderId);
        }
        request.setOrderId(orderId);
        Payment payment = paymentMapper.toEntity(request);
        payment.setAmount(order.getTotalAmount());
        payment.setOrder(order);

        Payment savedPayment = paymentRepository.save(payment);
        eventPublisher.publishEvent(new WebhookEvent(
                "PAYMENT_CREATED",
                "PAYMENT",
                savedPayment.getId(),
                userId
        ));
        Long voucherId = request.getVoucherId();
        BigDecimal discountAmount = request.getDiscountAmount();
        if (voucherId != null && discountAmount != null) {
            voucherUseService.createVoucherUse(voucherId, userId, orderId, discountAmount);
        }
        return paymentMapper.toResponse(savedPayment);
    }
}
