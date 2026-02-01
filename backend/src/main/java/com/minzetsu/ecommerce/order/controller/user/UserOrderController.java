package com.minzetsu.ecommerce.order.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.order.dto.request.OrderRequest;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users/me/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Orders", description = "Quản lý đơn hàng của người dùng hiện tại")
public class UserOrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Lấy danh sách đơn hàng của người dùng hiện tại",
            description = "Trả về danh sách tất cả các đơn hàng mà người dùng hiện tại đã đặt."
    )
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getCurrentUserOrders() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(orderService.getOrderResponsesByUserId(userId));
    }

    @Operation(
            summary = "Lấy thông tin chi tiết đơn hàng theo ID",
            description = "Trả về thông tin đầy đủ của đơn hàng (bao gồm sản phẩm, thanh toán, vận chuyển) của người dùng hiện tại."
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getCurrentUserOrderById(
            @PathVariable("orderId") Long orderId
    ) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(orderService.getFullOrderResponseByIdAndUserId(orderId, userId));
    }

    @Operation(
            summary = "Lấy số tiền giảm giá khi áp dụng voucher cho đơn hàng mới",
            description = "Tính toán và trả về số tiền giảm giá khi người dùng hiện tại áp dụng một voucher cụ thể cho đơn hàng mới."
    )
    @PostMapping("/voucher-discount")
    public ResponseEntity<BigDecimal> getVoucherDiscountForCurrentUserOrder(
            @Valid @RequestBody OrderRequest request
    ) {
        Long userId = getCurrentUserId();
        BigDecimal discountAmount = orderService.getDisCountAmount(request, userId);
        return ResponseEntity.ok(discountAmount);
    }

    @Operation(
            summary = "Tạo đơn hàng mới",
            description = "Người dùng hiện tại tạo một đơn hàng mới dựa trên thông tin trong yêu cầu (giỏ hàng, địa chỉ, phương thức thanh toán, v.v.)."
    )
    @PostMapping
    public ResponseEntity<OrderResponse> createCurrentUserOrder(
            @Valid @RequestBody OrderRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey
    ) {
        Long userId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrderResponse(request, userId, idempotencyKey));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
