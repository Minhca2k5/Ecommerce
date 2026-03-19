package com.minzetsu.ecommerce.payment.momo;

import com.minzetsu.ecommerce.common.exception.NotFoundException;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.common.exception.InvalidObjectException;
import com.minzetsu.ecommerce.common.utils.OutboundRetryExecutor;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.service.OrderService;
import com.minzetsu.ecommerce.payment.dto.request.PaymentRequest;
import com.minzetsu.ecommerce.payment.dto.response.PaymentResponse;
import com.minzetsu.ecommerce.payment.entity.PaymentStatus;
import com.minzetsu.ecommerce.payment.momo.dto.request.MomoCreateRequest;
import com.minzetsu.ecommerce.payment.momo.dto.response.MomoCreateResponse;
import com.minzetsu.ecommerce.payment.momo.dto.request.MomoIpnRequest;
import com.minzetsu.ecommerce.payment.service.PaymentService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;

@Service
public class MomoPaymentService {
    private final MomoProperties properties;
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final RestTemplate restTemplate;
    private final OutboundRetryExecutor retryExecutor;

    public MomoPaymentService(
            MomoProperties properties,
            PaymentService paymentService,
            OrderService orderService,
            RestTemplate restTemplate,
            OutboundRetryExecutor retryExecutor
    ) {
        this.properties = properties;
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.restTemplate = restTemplate;
        this.retryExecutor = retryExecutor;
    }

    public MomoCreateResponse createPayment(Long orderId, Long userId, String idempotencyKey) {
        validateConfig();
        Order order = orderService.getOrderByIdAndUserId(orderId, userId);
        String orderInfo = buildOrderInfo(orderId);
        String amount = formatAmount(order.getTotalAmount());
        String extraData = properties.getExtraData() == null ? "" : properties.getExtraData();

        String momoOrderId = "order_" + orderId + "_" + System.currentTimeMillis();

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(orderId)
                .method("MOMO")
                .status("INITIATED")
                .providerTxnId(momoOrderId)
                .build();
        PaymentResponse paymentResponse = paymentService.createPaymentResponse(paymentRequest, userId, orderId, idempotencyKey);
        String requestId = String.valueOf(paymentResponse.getId());

        MomoCreateRequest momoRequest = new MomoCreateRequest();
        momoRequest.setPartnerCode(properties.getPartnerCode());
        momoRequest.setAccessKey(properties.getAccessKey());
        momoRequest.setRequestId(requestId);
        momoRequest.setAmount(amount);
        momoRequest.setOrderId(momoOrderId);
        momoRequest.setOrderInfo(orderInfo);
        momoRequest.setRedirectUrl(properties.getRedirectUrl());
        momoRequest.setIpnUrl(properties.getIpnUrl());
        momoRequest.setExtraData(extraData);
        momoRequest.setRequestType(properties.getRequestType());
        momoRequest.setSignature(signCreateRequest(momoRequest));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MomoCreateRequest> entity = new HttpEntity<>(momoRequest, headers);

        MomoCreateResponse response = retryExecutor.execute(() ->
                restTemplate.postForObject(properties.getEndpoint() + "/v2/gateway/api/create", entity, MomoCreateResponse.class)
        );
        if (response == null || (response.getResultCode() != null && response.getResultCode() != 0)) {
            paymentService.updatePaymentStatusById(PaymentStatus.FAILED, paymentResponse.getId());
            String message = response == null ? "MoMo create payment failed." : response.getMessage();
            throw new InvalidObjectException(message == null || message.isBlank() ? "MoMo create payment failed." : message);
        }
        return response;
    }

    public void handleIpn(MomoIpnRequest ipn) {
        if (!verifyIpnSignature(ipn)) {
            throw new UnAuthorizedException("Invalid MoMo signature");
        }
        Long paymentId = parseLong(ipn.getRequestId());
        if (paymentId == null && ipn.getOrderId() != null && !ipn.getOrderId().isBlank()) {
            paymentId = paymentService.getPaymentByProviderTxnId(ipn.getOrderId()).getId();
        }
        if (paymentId == null) throw new NotFoundException("Payment id not found");
        if (Integer.valueOf(0).equals(ipn.getResultCode())) {
            paymentService.updatePaymentStatusById(PaymentStatus.SUCCEEDED, paymentId);
        } else {
            paymentService.updatePaymentStatusById(PaymentStatus.FAILED, paymentId);
        }
    }


    private String signCreateRequest(MomoCreateRequest request) {
        String rawSignature = "accessKey=" + request.getAccessKey()
                + "&amount=" + request.getAmount()
                + "&extraData=" + request.getExtraData()
                + "&ipnUrl=" + request.getIpnUrl()
                + "&orderId=" + request.getOrderId()
                + "&orderInfo=" + request.getOrderInfo()
                + "&partnerCode=" + request.getPartnerCode()
                + "&redirectUrl=" + request.getRedirectUrl()
                + "&requestId=" + request.getRequestId()
                + "&requestType=" + request.getRequestType();
        return MomoSignatureUtil.hmacSha256(properties.getSecretKey(), rawSignature);
    }

    private boolean verifyIpnSignature(MomoIpnRequest ipn) {
        if (ipn.getSignature() == null || ipn.getSignature().isBlank()) {
            return false;
        }
        String rawSignature = "accessKey=" + properties.getAccessKey()
                + "&amount=" + ipn.getAmount()
                + "&extraData=" + safe(ipn.getExtraData())
                + "&message=" + safe(ipn.getMessage())
                + "&orderId=" + ipn.getOrderId()
                + "&orderInfo=" + safe(ipn.getOrderInfo())
                + "&orderType=" + safe(ipn.getOrderType())
                + "&partnerCode=" + ipn.getPartnerCode()
                + "&payType=" + safe(ipn.getPayType())
                + "&requestId=" + ipn.getRequestId()
                + "&responseTime=" + safe(ipn.getResponseTime())
                + "&resultCode=" + String.valueOf(ipn.getResultCode())
                + "&transId=" + safe(ipn.getTransId());
        String expected = MomoSignatureUtil.hmacSha256(properties.getSecretKey(), rawSignature);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                ipn.getSignature().getBytes(StandardCharsets.UTF_8)
        );
    }

    private String buildOrderInfo(Long orderId) {
        String prefix = properties.getOrderInfoPrefix() == null ? "Order" : properties.getOrderInfoPrefix();
        return prefix + " #" + orderId;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return amount.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void validateConfig() {
        if (isBlank(properties.getPartnerCode())
                || isBlank(properties.getAccessKey())
                || isBlank(properties.getSecretKey())
                || isBlank(properties.getEndpoint())
                || isBlank(properties.getRedirectUrl())
                || isBlank(properties.getIpnUrl())
                || isBlank(properties.getRequestType())) {
            throw new InvalidObjectException("MoMo config missing. Please set momo.partnerCode, momo.accessKey, momo.secretKey, momo.endpoint, momo.redirectUrl, momo.ipnUrl, momo.requestType.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}


