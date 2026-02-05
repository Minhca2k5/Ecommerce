package com.minzetsu.ecommerce.order.controller.pub;

import com.minzetsu.ecommerce.order.dto.request.OrderRequest;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/checkout/guest")
@RequiredArgsConstructor
@Tag(name = "Public - Guest Checkout", description = "Checkout using guest cart without login")
public class PublicGuestCheckoutController {

    private final OrderService orderService;

    @Operation(summary = "Create order from guest cart")
    @PostMapping("/{guestId}")
    public ResponseEntity<OrderResponse> checkoutGuest(
            @PathVariable String guestId,
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createGuestOrderResponse(request, guestId, idempotencyKey));
    }
}
