package com.minzetsu.ecommerce.activity.controller.user;

import com.minzetsu.ecommerce.activity.dto.request.WishlistRequest;
import com.minzetsu.ecommerce.activity.dto.response.WishlistResponse;
import com.minzetsu.ecommerce.activity.service.WishlistService;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/wishlists")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Wishlist", description = "Quản lý danh sách yêu thích của người dùng")
public class UserWishlistController {

    private final WishlistService wishlistService;

    @Operation(
            summary = "Lấy danh sách wishlist của người dùng",
            description = "Trả về danh sách sản phẩm mà người dùng đã thêm vào wishlist. Hỗ trợ tìm kiếm theo tên sản phẩm và phân trang."
    )
    @ApiResponse(responseCode = "200", description = "Lấy wishlist thành công")
    @GetMapping
    public ResponseEntity<Page<WishlistResponse>> getWishlists(
            @RequestParam(required = false) String productName,
            Pageable pageable
    ) {
        Long userId = getCurrentUserId();
        if (productName != null && !productName.isEmpty()) {
             List<WishlistResponse> list = wishlistService.getWishlistsByProductName(productName, userId);
             return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(list, pageable, list.size()));
        }
        return ResponseEntity.ok(wishlistService.getWishlistByUserId(userId, pageable));
    }

    @Operation(
            summary = "Thêm sản phẩm vào wishlist",
            description = "Thêm sản phẩm vào danh sách yêu thích. Nếu sản phẩm đã tồn tại trong wishlist, hệ thống sẽ bỏ qua và trả về item đó."
    )
    @ApiResponse(responseCode = "200", description = "Thêm wishlist thành công")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm")
    @PostMapping
    public ResponseEntity<WishlistResponse> addWishlist(@Valid @RequestBody WishlistRequest request) {
        Long userId = getCurrentUserId();
        WishlistResponse wishlistResponse = wishlistService.addProductToWishlist(request, userId);
        return ResponseEntity.ok(wishlistResponse);
    }

    @Operation(
            summary = "Xóa toàn bộ wishlist",
            description = "Xóa tất cả sản phẩm yêu thích của người dùng."
    )
    @ApiResponse(responseCode = "200", description = "Xóa wishlist thành công")
    @DeleteMapping
    public ResponseEntity<Void> clearWishlist() {
        Long userId = getCurrentUserId();
        wishlistService.clearWishlistByUserId(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Xóa một item khỏi wishlist",
            description = "Xóa sản phẩm khỏi danh sách yêu thích theo wishlistId. Người dùng chỉ có thể xóa mục của chính họ."
    )
    @ApiResponse(responseCode = "200", description = "Xóa item trong wishlist thành công")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy wishlist item")
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<Void> removeWishlist(@PathVariable Long wishlistId) {
        Long userId = getCurrentUserId();
        wishlistService.removeProductFromWishlist(wishlistId, userId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
