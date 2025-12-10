package com.minzetsu.ecommerce.cart.controller.admin;

import com.minzetsu.ecommerce.cart.dto.response.CartResponse;
import com.minzetsu.ecommerce.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Carts", description = "Quản lý giỏ hàng của người dùng")
public class AdminCartController {

    private final CartService cartService;

    @Operation(
            summary = "Lấy giỏ hàng của người dùng",
            description = "Trả về thông tin đầy đủ giỏ hàng của người dùng dựa trên userId (bao gồm danh sách sản phẩm và chi tiết liên quan)."
    )
    @GetMapping("/{userId}/cart")
    public ResponseEntity<CartResponse> getCartByUserId(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(cartService.getFullCartResponseByUserId(userId));
    }
}
