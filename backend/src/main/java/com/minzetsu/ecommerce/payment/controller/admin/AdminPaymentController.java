package com.minzetsu.ecommerce.payment.controller.admin;

import com.minzetsu.ecommerce.payment.dto.filter.PaymentFilter;
import com.minzetsu.ecommerce.payment.dto.response.PaymentResponse;
import com.minzetsu.ecommerce.payment.entity.PaymentStatus;
import com.minzetsu.ecommerce.payment.service.PaymentService;
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
@RequestMapping("/api/admin/payments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Payments", description = "Quản lý thanh toán và trạng thái giao dịch của người dùng")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Tìm kiếm thanh toán",
            description = "Lọc và tìm kiếm các bản ghi thanh toán dựa trên điều kiện như orderId, trạng thái, hoặc phương thức thanh toán. Hỗ trợ phân trang."
    )
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> searchPayments(
            @ModelAttribute PaymentFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(paymentService.searchPaymentResponses(filter, pageable));
    }

    @Operation(
            summary = "Lấy danh sách thanh toán theo đơn hàng",
            description = "Trả về tất cả các bản ghi thanh toán liên quan đến một đơn hàng cụ thể."
    )
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(
            @PathVariable("orderId") Long orderId
    ) {
        return ResponseEntity.ok(paymentService.getPaymentResponsesByOrderId(orderId, null));
    }

    @Operation(
            summary = "Cập nhật trạng thái thanh toán",
            description = "Cập nhật trạng thái của một thanh toán cụ thể (VD: PENDING, SUCCESS, FAILED, REFUNDED, ...)."
    )
    @PatchMapping("/{paymentId}/status")
    public ResponseEntity<Void> updatePaymentStatus(
            @PathVariable("paymentId") Long paymentId,
            @RequestParam PaymentStatus status
    ) {
        paymentService.updatePaymentStatusById(status, paymentId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lấy thông tin chi tiết thanh toán",
            description = "Trả về thông tin chi tiết của một thanh toán dựa trên ID thanh toán."
    )
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(
            @PathVariable("paymentId") Long paymentId
    ) {
        return ResponseEntity.ok(paymentService.getPaymentResponseById(paymentId, null));
    }
}

