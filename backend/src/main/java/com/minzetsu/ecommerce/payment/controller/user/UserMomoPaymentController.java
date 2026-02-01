package com.minzetsu.ecommerce.payment.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.payment.momo.MomoPaymentService;
import com.minzetsu.ecommerce.payment.momo.dto.MomoCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/orders/{orderId}/payments/momo")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - MoMo Payment", description = "Create MoMo payment for current user order")
public class UserMomoPaymentController {
    private final MomoPaymentService momoPaymentService;

    @Operation(summary = "Create MoMo payment")
    @PostMapping("/create")
    public ResponseEntity<MomoCreateResponse> createMomoPayment(
            @PathVariable("orderId") Long orderId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(momoPaymentService.createPayment(orderId, userId, idempotencyKey));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
