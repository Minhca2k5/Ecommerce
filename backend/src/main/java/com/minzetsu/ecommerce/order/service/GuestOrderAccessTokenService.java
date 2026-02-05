package com.minzetsu.ecommerce.order.service;

import com.minzetsu.ecommerce.common.exception.UnAuthorizedException;
import com.minzetsu.ecommerce.order.config.GuestCheckoutProperties;
import com.minzetsu.ecommerce.order.entity.Order;
import com.minzetsu.ecommerce.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuestOrderAccessTokenService {

    private static final String HMAC_ALGO = "HmacSHA256";
    private static final String TOKEN_VERSION = "v1";
    private static final HexFormat HEX = HexFormat.of();

    private final GuestCheckoutProperties properties;
    private final GuestCheckoutIdentityService guestCheckoutIdentityService;
    private final OrderRepository orderRepository;

    public String issueToken(Order order) {
        long expiresAt = order.getCreatedAt()
                .plusMinutes(properties.getAccessTokenTtlMinutes())
                .toEpochSecond(ZoneOffset.UTC);
        String payload = order.getId() + ":" + expiresAt;
        String signature = sign(payload);
        return TOKEN_VERSION + "." + order.getId() + "." + expiresAt + "." + signature;
    }

    public Order authorizeGuestOrder(Long orderId, String token) {
        if (orderId == null || !StringUtils.hasText(token)) {
            throw new UnAuthorizedException("Guest access token is required");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 4 || !TOKEN_VERSION.equals(parts[0])) {
            throw new UnAuthorizedException("Invalid guest access token");
        }
        Long tokenOrderId;
        long expiresAt;
        try {
            tokenOrderId = Long.parseLong(parts[1]);
            expiresAt = Long.parseLong(parts[2]);
        } catch (NumberFormatException ex) {
            throw new UnAuthorizedException("Invalid guest access token");
        }
        if (!orderId.equals(tokenOrderId)) {
            throw new UnAuthorizedException("Guest access token does not match order");
        }
        if (Instant.now().isAfter(Instant.ofEpochSecond(expiresAt))) {
            throw new UnAuthorizedException("Guest access token is expired");
        }
        String payload = tokenOrderId + ":" + expiresAt;
        String expected = sign(payload);
        String provided = parts[3];
        if (!constantTimeEquals(expected, provided)) {
            throw new UnAuthorizedException("Invalid guest access token");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new UnAuthorizedException("Order not found"));
        Long guestUserId = guestCheckoutIdentityService.resolveGuestCheckoutUserId();
        if (order.getUser() == null || !guestUserId.equals(order.getUser().getId())) {
            throw new UnAuthorizedException("Order is not a guest checkout order");
        }
        return order;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            mac.init(new SecretKeySpec(properties.getAccessTokenSecret().getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(raw);
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot sign guest token payload", ex);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] a = left.getBytes(StandardCharsets.UTF_8);
        byte[] b = right.getBytes(StandardCharsets.UTF_8);
        if (a.length != b.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }
}
