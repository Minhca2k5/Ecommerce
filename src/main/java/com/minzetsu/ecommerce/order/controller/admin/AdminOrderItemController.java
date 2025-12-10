package com.minzetsu.ecommerce.order.controller.admin;

import com.minzetsu.ecommerce.order.dto.filter.OrderItemFilter;
import com.minzetsu.ecommerce.order.dto.response.OrderItemResponse;
import com.minzetsu.ecommerce.order.service.OrderItemService;
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
@RequestMapping("/api/admin/order-items")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Order Items", description = "Quản lý chi tiết sản phẩm trong các đơn hàng của người dùng")
public class AdminOrderItemController {

    private final OrderItemService orderItemService;

    @Operation(
            summary = "Tìm kiếm chi tiết đơn hàng",
            description = "Lọc và tìm kiếm các mục sản phẩm trong đơn hàng dựa trên điều kiện như orderId, productId, v.v. Hỗ trợ phân trang."
    )
    @GetMapping
    public ResponseEntity<Page<OrderItemResponse>> searchOrderItems(
            @ModelAttribute OrderItemFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(orderItemService.searchOrderItemResponses(filter, pageable));
    }

    @Operation(
            summary = "Lấy danh sách chi tiết đơn hàng theo ID đơn hàng",
            description = "Trả về danh sách tất cả các mục sản phẩm thuộc một đơn hàng cụ thể."
    )
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemResponse>> getOrderItemsByOrderId(
            @PathVariable("orderId") Long orderId
    ) {
        return ResponseEntity.ok(orderItemService.getOrderItemResponsesByOrderIdAndUserId(orderId, null));
    }

    @Operation(
            summary = "Lấy thông tin chi tiết của một mục trong đơn hàng",
            description = "Trả về thông tin chi tiết của một sản phẩm trong đơn hàng dựa trên orderItemId."
    )
    @GetMapping("/{orderItemId}")
    public ResponseEntity<OrderItemResponse> getOrderItemById(
            @PathVariable("orderItemId") Long orderItemId
    ) {
        return ResponseEntity.ok(orderItemService.getOrderItemResponseByIdAndUserId(orderItemId, null));
    }
}
