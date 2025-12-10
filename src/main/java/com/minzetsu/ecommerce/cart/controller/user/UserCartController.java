package com.minzetsu.ecommerce.cart.controller.user;

import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/carts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Cart", description = "Quản lý giỏ hàng của người dùng hiện tại (đăng nhập)")
public class UserCartController {

    private final CartService cartService;

    @Operation(
            summary = "Lấy giỏ hàng của người dùng hiện tại",
            description = "Trả về thông tin đầy đủ giỏ hàng của người dùng đang đăng nhập (bao gồm chi tiết sản phẩm)."
    )
    @GetMapping
    public ResponseEntity<CartResponse> getCurrentUserCart() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.getFullCartResponseByUserId(userId));
    }

    @Operation(
            summary = "Tạo giỏ hàng mới cho người dùng hiện tại",
            description = "Khởi tạo giỏ hàng mới cho người dùng đang đăng nhập nếu chưa có."
    )
    @PostMapping
    public ResponseEntity<CartResponse> createCurrentUserCart() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.createCartResponse(userId));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
