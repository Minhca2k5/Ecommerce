package com.minzetsu.ecommerce.payment.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.payment.dto.request.PaymentRequest;
import com.minzetsu.ecommerce.payment.dto.response.PaymentResponse;
import com.minzetsu.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/orders/{orderId}/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Payments", description = "Quản lý thanh toán của người dùng hiện tại cho từng đơn hàng")
public class UserPaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Lấy danh sách thanh toán cho đơn hàng",
            description = "Trả về tất cả các bản ghi thanh toán của người dùng hiện tại cho đơn hàng cụ thể."
    )
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getCurrentUserPaymentsByOrderId(
            @PathVariable("orderId") Long orderId
    ) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(paymentService.getPaymentResponsesByOrderId(orderId, userId));
    }

    @Operation(
            summary = "Lấy chi tiết thanh toán trong đơn hàng",
            description = "Trả về thông tin chi tiết của một thanh toán cụ thể thuộc về đơn hàng của người dùng hiện tại."
    )
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getCurrentUserPaymentById(
            @PathVariable("paymentId") Long paymentId
    ) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(paymentService.getPaymentResponseById(paymentId, userId));
    }

    @Operation(
            summary = "Tạo thanh toán mới cho đơn hàng",
            description = "Người dùng hiện tại tạo một thanh toán mới cho đơn hàng của mình (VD: thanh toán bằng thẻ, ví điện tử...)."
    )
    @PostMapping
    public ResponseEntity<PaymentResponse> createCurrentUserPayment(
            @PathVariable("orderId") Long orderId,
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        Long userId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPaymentResponse(request, userId, orderId, idempotencyKey));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnAuthorizedException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}

