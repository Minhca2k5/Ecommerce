package com.minzetsu.ecommerce.cart.controller.user;

import com.minzetsu.ecommerce.cart.dto.request.CartItemRequest;
import com.minzetsu.ecommerce.cart.dto.response.CartItemResponse;
import com.minzetsu.ecommerce.cart.service.CartItemService;
import com.minzetsu.ecommerce.cart.service.CartService;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/carts/{cartId}/items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Cart Items", description = "Quản lý các sản phẩm trong giỏ hàng của người dùng hiện tại")
public class UserCartItemController {

    private final CartItemService cartItemService;
    private final CartService cartService;

    @Operation(summary = "Lấy danh sách sản phẩm trong giỏ hàng (phân trang)", description = "Trả về danh sách các sản phẩm trong giỏ hàng của người dùng hiện tại, hỗ trợ tìm kiếm theo tên sản phẩm và phân trang.")
    @GetMapping
    public ResponseEntity<Page<CartItemResponse>> getCurrentUserCartItems(
            @RequestParam(required = false) String productName,
            Pageable pageable) {
        Long userId = getCurrentUserId();
        if (productName != null && !productName.isEmpty()) {
            List<CartItemResponse> list = cartItemService.getCarItemResponsesByProductName(productName, userId);
            return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(list, pageable, list.size()));
        }
        return ResponseEntity.ok(cartItemService.getCartItemResponsesByCartId(null, userId, pageable));
    }

    @Operation(summary = "Thêm hoặc cập nhật sản phẩm trong giỏ hàng", description = "Thêm sản phẩm mới vào giỏ hàng hoặc cập nhật nếu sản phẩm đã tồn tại.")
    @PostMapping
    public ResponseEntity<CartItemResponse> addOrUpdateCurrentUserCartItem(
            @Valid @RequestBody CartItemRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartItemService.addOrUpdateCartItemResponse(request, false, userId));
    }

    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ hàng khi hoàn trả", description = "Điều chỉnh số lượng sản phẩm trong giỏ hàng (dùng khi người dùng hoàn trả sản phẩm).")
    @PutMapping("/return")
    public ResponseEntity<CartItemResponse> updateCurrentUserCartItemQuantity(
            @Valid @RequestBody CartItemRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(cartItemService.addOrUpdateCartItemResponse(request, true, userId));
    }

    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng", description = "Xóa một sản phẩm cụ thể khỏi giỏ hàng của người dùng hiện tại.")
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCurrentUserCartItem(@PathVariable Long cartItemId) {
        Long userId = getCurrentUserId();
        cartItemService.deleteById(cartItemService.getCartItemByIdAndUserId(cartItemId, userId).getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Xóa toàn bộ giỏ hàng", description = "Xóa toàn bộ sản phẩm trong giỏ hàng của người dùng hiện tại.")
    @DeleteMapping
    public ResponseEntity<Void> clearCurrentUserCart() {
        Long userId = getCurrentUserId();
        cartItemService.deleteByCartId(cartService.getCartByUserId(userId).getId());
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.minzetsu.ecommerce.common.exception.UnAuthorizedException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
