package com.minzetsu.ecommerce.payment.service.impl;

import com.minzetsu.ecommerce.common.exception.AlreadyExistException;
import com.minzetsu.ecommerce.common.audit.entity.AuditAction;
import com.minzetsu.ecommerce.common.exception.AppException;
import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.common.utils.DatabaseRetryExecutor;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.common.idempotency.service.IdempotencyService;
import com.minzetsu.ecommerce.messaging.event.DomainEventPublisher;
import com.minzetsu.ecommerce.messaging.event.DomainEventType;
import com.minzetsu.ecommerce.mongo.service.ClickstreamEventService;
import com.minzetsu.ecommerce.notification.dto.request.NotificationCreateRequest;
import com.minzetsu.ecommerce.notification.service.NotificationService;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.entity.OrderStatus;
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
import com.minzetsu.ecommerce.realtime.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService;
    private final VoucherUseService voucherUseService;
    private final ApplicationEventPublisher eventPublisher;
    private final DomainEventPublisher domainEventPublisher;
    private final IdempotencyService idempotencyService;
    private final NotificationService notificationService;
    private final SseEmitterService sseEmitterService;
    private final DatabaseRetryExecutor databaseRetryExecutor;
    private final PlatformTransactionManager transactionManager;
    private final ClickstreamEventService clickstreamEventService;

    private Payment getExistingPayment(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Payment not found with id: " + id));
    }

    @Override
    @Transactional
    @AuditAction(action = "PAYMENT_STATUS_UPDATED", entityType = "PAYMENT", idParamIndex = 1)
    public void updatePaymentStatusById(PaymentStatus status, Long id) {
        Payment payment = getExistingPayment(id);
        if (payment.getStatus() != PaymentStatus.INITIATED) {
            throw new AppException("Only pending payments can be updated by admin", HttpStatus.BAD_REQUEST);
        }
        paymentRepository.updateByStatusAndId(status, id);
        payment.setStatus(status);
        syncOrderStatusFromPayment(payment, status);
        eventPublisher.publishEvent(new WebhookEvent(
                "PAYMENT_STATUS_UPDATED",
                "PAYMENT",
                id,
                null
        ));
        if (status == PaymentStatus.SUCCEEDED) {
            domainEventPublisher.publish(DomainEventType.PAYMENT_SUCCEEDED, id, null, Map.of("status", status.name()));
            Long userId = payment.getOrder() != null && payment.getOrder().getUser() != null
                    ? payment.getOrder().getUser().getId()
                    : null;
            clickstreamEventService.recordPaymentSuccess(userId);
        }
        notifyPaymentStatus(payment, status);
    }

    private void syncOrderStatusFromPayment(Payment payment, PaymentStatus paymentStatus) {
        Order order = payment.getOrder();
        if (order == null || order.getId() == null) {
            return;
        }

        OrderStatus targetStatus = switch (paymentStatus) {
            case SUCCEEDED -> OrderStatus.PAID;
            case FAILED -> OrderStatus.FAILED;
            default -> null;
        };

        if (targetStatus == null || order.getStatus() == targetStatus) {
            return;
        }

        orderService.updateOrderStatus(order.getId(), targetStatus);
        order.setStatus(targetStatus);
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
    public Payment getPaymentByProviderTxnId(String providerTxnId) {
        if (providerTxnId == null || providerTxnId.isBlank()) {
            throw new NotFoundException("Payment provider transaction id is missing");
        }
        Payment payment = paymentRepository.findByProviderTxnId(providerTxnId);
        if (payment == null) {
            throw new NotFoundException("Payment not found for provider transaction id: " + providerTxnId);
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
    @AuditAction(action = "PAYMENT_CREATED", entityType = "PAYMENT")
    public PaymentResponse createPaymentResponse(PaymentRequest request, Long userId, Long orderId, String idempotencyKey) {
        return databaseRetryExecutor.execute(
                "payment-create",
                () -> withWriteTransaction(() -> idempotencyService.execute(
                        idempotencyKey,
                        "PAYMENT_CREATE",
                        userId,
                        "PAYMENT",
                        id -> getPaymentResponseById(id, userId),
                        () -> createPaymentInternal(request, userId, orderId),
                        PaymentResponse::getId
                ))
        );
    }

    private <T> T withWriteTransaction(Supplier<T> supplier) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        return template.execute(status -> supplier.get());
    }

    private PaymentResponse createPaymentInternal(PaymentRequest request, Long userId, Long orderId) {
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
        domainEventPublisher.publish(DomainEventType.PAYMENT_CREATED, savedPayment.getId(), userId, Map.of());
        notifyPaymentCreated(savedPayment, userId);
        Long voucherId = order.getVoucher() != null ? order.getVoucher().getId() : null;
        BigDecimal discountAmount = order.getDiscountAmount();
        if (voucherId != null
                && discountAmount != null
                && discountAmount.compareTo(BigDecimal.ZERO) > 0
                && !voucherUseService.existsByOrderId(orderId)) {
            voucherUseService.createVoucherUse(voucherId, userId, orderId, discountAmount);
        }
        return paymentMapper.toResponse(savedPayment);
    }

    private void notifyPaymentCreated(Payment payment, Long userId) {
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .userId(userId)
                .title("Payment created")
                .message("Your payment has been created.")
                .type("PAYMENT")
                .referenceId(payment.getOrder().getId().intValue())
                .referenceType("ORDER")
                .build();
        notificationService.createNotificationResponse(request, userId);
        sseEmitterService.sendToUser(userId, "payment-created", Map.of(
                "paymentId", payment.getId(),
                "orderId", payment.getOrder().getId()
        ));
        sseEmitterService.sendToAdmins("payment-created", Map.of(
                "paymentId", payment.getId(),
                "orderId", payment.getOrder().getId(),
                "userId", userId
        ));
    }

    private void notifyPaymentStatus(Payment payment, PaymentStatus status) {
        Long userId = payment.getOrder().getUser().getId();
        String title = status == PaymentStatus.SUCCEEDED ? "Payment succeeded" : "Payment failed";
        String message = status == PaymentStatus.SUCCEEDED
                ? "Your payment was successful."
                : "Your payment failed. Please try again.";
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type("PAYMENT")
                .referenceId(payment.getOrder().getId().intValue())
                .referenceType("ORDER")
                .build();
        notificationService.createNotificationResponse(request, userId);
        sseEmitterService.sendToUser(userId, "payment-status", Map.of(
                "paymentId", payment.getId(),
                "orderId", payment.getOrder().getId(),
                "status", status.name()
        ));
        sseEmitterService.sendToAdmins("payment-status", Map.of(
                "paymentId", payment.getId(),
                "orderId", payment.getOrder().getId(),
                "userId", userId,
                "status", status.name()
        ));
    }
}


