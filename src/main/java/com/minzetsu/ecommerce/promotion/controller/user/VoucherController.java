package com.minzetsu.ecommerce.promotion.controller.user;

import com.minzetsu.ecommerce.promotion.dto.response.UserVoucherResponse;
import com.minzetsu.ecommerce.promotion.entity.VoucherStatus;
import com.minzetsu.ecommerce.promotion.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users/me/vouchers")
@RequiredArgsConstructor
@Tag(
        name = "User - Vouchers",
        description = "Quản lý voucher dành cho người dùng"
)
public class VoucherController {
    private final VoucherService voucherService;

    @Operation(
            summary = "Lấy danh sách voucher theo code",
            description = "Lấy danh sách voucher với khả năng lọc, phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy danh sách voucher thành công")
    @GetMapping
    public ResponseEntity<List<UserVoucherResponse>> getVouchersByCode(@RequestParam String code) {
        List<UserVoucherResponse> vouchers = voucherService.getUserVoucherResponsesByCodeAndActiveStatus(code, VoucherStatus.ACTIVE);
        return ResponseEntity.ok(vouchers);
    }

    @Operation(
            summary = "Lấy danh sách voucher theo số tiền đơn hàng tối thiểu",
            description = "Lấy danh sách voucher với khả năng lọc, phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy danh sách voucher thành công")
    @GetMapping("/filter")
    public ResponseEntity<Page<UserVoucherResponse>> getVouchersByMinOrderAmount(
            @RequestParam BigDecimal minOrderAmount,
            Pageable pageable
            ) {
        Page<UserVoucherResponse> vouchers = voucherService.searchUserVoucherResponsesByMinOrderTotalAndActiveStatus(minOrderAmount, VoucherStatus.ACTIVE, pageable);
        return ResponseEntity.ok(vouchers);
    }

    @Operation(
            summary = "Lấy thông tin voucher theo ID",
            description = "Trả về thông tin cơ bản của một voucher (chỉ dữ liệu chính, không có chi tiết liên kết)."
    )
    @ApiResponse(responseCode = "200", description = "Lấy thông tin voucher thành công")
    @GetMapping("/{voucherId}")
    public ResponseEntity<UserVoucherResponse> getVoucherById(
            @PathVariable("voucherId") Long voucherId
    ) {
        return ResponseEntity.ok(voucherService.getUserVoucherResponseById(voucherId));
    }
}
