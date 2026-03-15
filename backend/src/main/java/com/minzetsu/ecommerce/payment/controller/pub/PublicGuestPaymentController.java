package com.minzetsu.ecommerce.payment.controller.pub;

import com.minzetsu.ecommerce.order.service.GuestCheckoutIdentityService;
import com.minzetsu.ecommerce.order.service.GuestOrderAccessTokenService;
import com.minzetsu.ecommerce.payment.dto.response.PaymentResponse;
import com.minzetsu.ecommerce.payment.momo.MomoPaymentService;
import com.minzetsu.ecommerce.payment.momo.dto.response.MomoCreateResponse;
import com.minzetsu.ecommerce.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/guest/orders/{orderId}/payments")
@RequiredArgsConstructor
@Tag(name = "Public - Guest Payments", description = "Guest payment operations using guest access token")
public class PublicGuestPaymentController {

    private final PaymentService paymentService;
    private final MomoPaymentService momoPaymentService;
    private final GuestOrderAccessTokenService guestOrderAccessTokenService;
    private final GuestCheckoutIdentityService guestCheckoutIdentityService;

    @Operation(summary = "List payments for guest order")
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> listPayments(
            @PathVariable Long orderId,
            @RequestParam(value = "accessToken", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Access-Token", required = false) String accessTokenHeader
    ) {
        String token = (accessToken != null && !accessToken.isBlank()) ? accessToken : accessTokenHeader;
        guestOrderAccessTokenService.authorizeGuestOrder(orderId, token);
        return ResponseEntity.ok(paymentService.getPaymentResponsesByOrderId(orderId, null));
    }

    @Operation(summary = "Create MoMo payment for guest order")
    @PostMapping("/momo/create")
    public ResponseEntity<MomoCreateResponse> createMomo(
            @PathVariable Long orderId,
            @RequestParam(value = "accessToken", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Access-Token", required = false) String accessTokenHeader,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        String token = (accessToken != null && !accessToken.isBlank()) ? accessToken : accessTokenHeader;
        guestOrderAccessTokenService.authorizeGuestOrder(orderId, token);
        Long guestUserId = guestCheckoutIdentityService.resolveGuestCheckoutUserId();
        return ResponseEntity.ok(momoPaymentService.createPayment(orderId, guestUserId, idempotencyKey));
    }
}


