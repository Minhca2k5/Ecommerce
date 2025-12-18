package com.minzetsu.ecommerce.promotion.controller.admin;

import com.minzetsu.ecommerce.promotion.dto.filter.VoucherFilter;
import com.minzetsu.ecommerce.promotion.dto.request.VoucherCreateRequest;
import com.minzetsu.ecommerce.promotion.dto.request.VoucherUpdateRequest;
import com.minzetsu.ecommerce.promotion.dto.response.AdminVoucherResponse;
import com.minzetsu.ecommerce.promotion.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Vouchers", description = "Quản lý voucher dành cho quản trị viên")
public class AdminVoucherController {
    private final VoucherService voucherService;

    @Operation(
            summary = "Lấy danh sách voucher",
            description = "Lấy danh sách voucher với khả năng lọc, phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy danh sách voucher thành công")
    @GetMapping
    public ResponseEntity<Page<AdminVoucherResponse>> getVouchers(
            @ModelAttribute VoucherFilter filter,
            Pageable pageable
            ){
        Page<AdminVoucherResponse> vouchers = voucherService.searchAdminVoucherResponses(filter, pageable);
        return ResponseEntity.ok(vouchers);
    }

    @Operation(
            summary = "Tạo voucher mới",
            description = "Tạo một voucher mới với các thông tin được cung cấp."
    )
    @ApiResponse(responseCode = "200", description = "Tạo voucher thành công")
    @PostMapping
    public ResponseEntity<AdminVoucherResponse> createVoucher(@Valid @RequestBody VoucherCreateRequest request) {
        AdminVoucherResponse createdVoucher = voucherService.createAdminVoucherResponse(request);
        return ResponseEntity.ok(createdVoucher);
    }

    @Operation(
            summary = "Cập nhật voucher",
            description = "Cập nhật thông tin của một voucher hiện có."
    )
    @ApiResponse(responseCode = "200", description = "Cập nhật voucher thành công")
    @PutMapping("/{id}")
    public ResponseEntity<AdminVoucherResponse> updateVoucher(
            @PathVariable("id") Long id,
            @RequestBody VoucherUpdateRequest request) {
        AdminVoucherResponse updatedVoucher = voucherService.updateAdminVoucherResponse(id, request);
        return ResponseEntity.ok(updatedVoucher);
    }

    @Operation(
            summary = "Xóa voucher",
            description = "Xóa một voucher dựa trên ID.")
    @ApiResponse(responseCode = "204", description = "Xóa voucher thành công")
    @DeleteMapping("/{voucherId}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long voucherId) {
        voucherService.deleteVoucher(voucherId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lấy thông tin voucher theo ID",
            description = "Trả về thông tin chi tiết của voucher dựa trên ID.")
    @ApiResponse(responseCode = "200", description = "Lấy thông tin voucher thành công")
    @GetMapping("/{voucherId}")
    public ResponseEntity<AdminVoucherResponse> getVoucherById(@PathVariable Long voucherId) {
        AdminVoucherResponse voucher = voucherService.getAdminVoucherResponseById(voucherId);
        return ResponseEntity.ok(voucher);
    }
}
