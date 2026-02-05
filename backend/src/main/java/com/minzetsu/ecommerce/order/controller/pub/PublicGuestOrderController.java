package com.minzetsu.ecommerce.order.controller.pub;

import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.service.GuestCheckoutIdentityService;
import com.minzetsu.ecommerce.order.service.GuestOrderAccessTokenService;
import com.minzetsu.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/guest/orders")
@RequiredArgsConstructor
@Tag(name = "Public - Guest Orders", description = "Guest order lookup using access token")
public class PublicGuestOrderController {

    private final OrderService orderService;
    private final GuestOrderAccessTokenService guestOrderAccessTokenService;
    private final GuestCheckoutIdentityService guestCheckoutIdentityService;

    @Operation(summary = "Get guest order detail")
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getGuestOrder(
            @PathVariable Long orderId,
            @RequestParam(value = "accessToken", required = false) String accessToken,
            @RequestHeader(value = "X-Guest-Access-Token", required = false) String accessTokenHeader
    ) {
        String token = (accessToken != null && !accessToken.isBlank()) ? accessToken : accessTokenHeader;
        guestOrderAccessTokenService.authorizeGuestOrder(orderId, token);
        Long guestUserId = guestCheckoutIdentityService.resolveGuestCheckoutUserId();
        OrderResponse response = orderService.getFullOrderResponseByIdAndUserId(orderId, guestUserId);
        response.setGuestAccessToken(token);
        return ResponseEntity.ok(response);
    }
}
