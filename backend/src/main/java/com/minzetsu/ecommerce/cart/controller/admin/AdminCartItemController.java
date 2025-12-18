package com.minzetsu.ecommerce.cart.controller.admin;

import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Cart Items", description = "Quản lý các mục trong giỏ hàng")
public class AdminCartItemController {

    private final CartItemService cartItemService;

    @Operation(
            summary = "Lấy chi tiết mục giỏ hàng",
            description = "Trả về thông tin chi tiết của một mục trong giỏ hàng theo ID."
    )
    @GetMapping("/cart-items/{cartItemId}")
    public ResponseEntity<CartItemResponse> getCartItemById(
            @PathVariable("cartItemId") Long cartItemId
    ) {
        return ResponseEntity.ok(cartItemService.getCartItemResponseByIdAndUserId(cartItemId, null));
    }

    @Operation(
            summary = "Lấy danh sách mục giỏ hàng theo Cart ID (phân trang)",
            description = "Trả về danh sách các mục giỏ hàng thuộc về giỏ hàng có ID tương ứng, hỗ trợ phân trang."
    )
    @GetMapping("/carts/{cartId}/items")
    public ResponseEntity<Page<CartItemResponse>> getPagedCartItemsByCartId(
            @PathVariable("cartId") Long cartId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(cartItemService.getCartItemResponsesByCartId(cartId, null, pageable));
    }

    @Operation(
            summary = "Lấy toàn bộ mục giỏ hàng theo Cart ID",
            description = "Trả về toàn bộ danh sách các mục giỏ hàng thuộc về giỏ hàng có ID tương ứng (không phân trang)."
    )
    @GetMapping("/carts/{cartId}/items/all")
    public ResponseEntity<List<CartItemResponse>> getAllCartItemsByCartId(
            @PathVariable("cartId") Long cartId
    ) {
        return ResponseEntity.ok(cartItemService.getCartItemResponsesByCartId(cartId, null));
    }
}
