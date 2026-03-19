package com.minzetsu.ecommerce.order.controller.admin;

import com.minzetsu.ecommerce.order.dto.filter.OrderFilter;
import com.minzetsu.ecommerce.order.dto.response.OrderResponse;
import com.minzetsu.ecommerce.order.entity.OrderStatus;
import com.minzetsu.ecommerce.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Orders", description = "Quản lý đơn hàng và trạng thái thanh toán của người dùng")
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(
            summary = "Tìm kiếm đơn hàng",
            description = "Lọc và tìm kiếm các đơn hàng theo trạng thái, người dùng hoặc các tiêu chí khác. Hỗ trợ phân trang."
    )
    @GetMapping
    public ResponseEntity<Page<OrderResponse>> searchOrders(
            @ModelAttribute OrderFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderService.searchOrderResponses(filter, pageable));
    }

    @Operation(
            summary = "Lấy thông tin chi tiết đơn hàng theo ID",
            description = "Trả về thông tin đầy đủ của đơn hàng, bao gồm danh sách sản phẩm, thanh toán và giao hàng."
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable("orderId") Long orderId) {
        return ResponseEntity.ok(orderService.getFullOrderResponseByIdAndUserId(orderId, null));
    }

    @Operation(
            summary = "Cập nhật trạng thái đơn hàng",
            description = "Cập nhật trạng thái đơn hàng (PENDING, SHIPPED, DELIVERED, CANCELED, v.v.) dựa trên orderId."
    )
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam OrderStatus status
    ) {
        orderService.updateAdminOrderStatus(orderId, status);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Cập nhật đơn vị tiền tệ của đơn hàng",
            description = "Cập nhật loại tiền tệ được sử dụng cho đơn hàng (VD: USD, VND, EUR...)."
    )
    @PatchMapping("/{orderId}/currency")
    public ResponseEntity<Void> updateOrderCurrency(
            @PathVariable("orderId") Long orderId,
            @RequestParam String currency
    ) {
        orderService.updateOrderCurrency(orderId, currency);
        return ResponseEntity.noContent().build();
    }
}
