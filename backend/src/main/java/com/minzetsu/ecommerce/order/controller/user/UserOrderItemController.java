package com.minzetsu.ecommerce.order.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.order.dto.response.OrderItemResponse;
import com.minzetsu.ecommerce.order.service.OrderItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/users/me/orders/{orderId}/items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Order Items", description = "Xem chi tiết sản phẩm trong các đơn hàng của người dùng hiện tại")
public class UserOrderItemController {

    private final OrderItemService orderItemService;

    @Operation(summary = "Lấy thông tin chi tiết của một sản phẩm trong đơn hàng", description = "Trả về thông tin chi tiết của một mục đơn hàng (sản phẩm) thuộc đơn hàng của người dùng hiện tại.")
    @GetMapping("/{orderItemId}")
    public ResponseEntity<OrderItemResponse> getCurrentUserOrderItemById(
            @PathVariable("orderItemId") Long orderItemId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(orderItemService.getOrderItemResponseByIdAndUserId(orderItemId, userId));
    }

    @Operation(summary = "Lấy toàn bộ danh sách sản phẩm trong đơn hàng", description = "Trả về danh sách tất cả các sản phẩm trong đơn hàng của người dùng hiện tại (không phân trang).")
    @GetMapping("/all")
    public ResponseEntity<List<OrderItemResponse>> getAllCurrentUserOrderItems(
            @PathVariable("orderId") Long orderId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(orderItemService.getOrderItemResponsesByOrderIdAndUserId(orderId, userId));
    }

    @Operation(summary = "Lấy danh sách sản phẩm trong đơn hàng (phân trang)", description = "Trả về danh sách các sản phẩm trong đơn hàng của người dùng hiện tại, hỗ trợ phân trang.")
    @GetMapping
    public ResponseEntity<Page<OrderItemResponse>> getPagedCurrentUserOrderItems(
            @PathVariable("orderId") Long orderId,
            Pageable pageable) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(orderItemService.getOrderItemResponsesByOrderIdAndUserId(orderId, userId, pageable));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.minzetsu.ecommerce.common.exception.UnAuthorizedException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
