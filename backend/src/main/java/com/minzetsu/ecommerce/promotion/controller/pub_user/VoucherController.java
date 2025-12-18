package com.minzetsu.ecommerce.promotion.controller.pub_user;

import com.minzetsu.ecommerce.promotion.dto.response.VoucherResponse;
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
@RequestMapping("/api/public/vouchers")
@RequiredArgsConstructor
@Tag(
        name = "Vouchers",
        description = "Quản lý voucher"
)
public class VoucherController {
    private final VoucherService voucherService;

    @Operation(
            summary = "Lấy danh sách voucher theo code",
            description = "Lấy danh sách voucher với khả năng lọc, phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy danh sách voucher thành công")
    @GetMapping
    public ResponseEntity<List<VoucherResponse>> getVouchersByCode(@RequestParam String code) {
        List<VoucherResponse> vouchers = voucherService.getVoucherResponsesByCodeAndActiveStatus(code, VoucherStatus.ACTIVE);
        return ResponseEntity.ok(vouchers);
    }

    @Operation(
            summary = "Lấy danh sách voucher theo số tiền đơn hàng tối thiểu",
            description = "Lấy danh sách voucher với khả năng lọc, phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy danh sách voucher thành công")
    @GetMapping("/filter")
    public ResponseEntity<Page<VoucherResponse>> getVouchersByMinOrderAmount(
            @RequestParam BigDecimal minOrderAmount,
            Pageable pageable
            ) {
        Page<VoucherResponse> vouchers = voucherService.searchVoucherResponsesByMinOrderTotalAndActiveStatus(minOrderAmount, VoucherStatus.ACTIVE, pageable);
        return ResponseEntity.ok(vouchers);
    }

    @Operation(
            summary = "Lấy thông tin voucher theo ID",
            description = "Trả về thông tin cơ bản của một voucher (chỉ dữ liệu chính, không có chi tiết liên kết)."
    )
    @ApiResponse(responseCode = "200", description = "Lấy thông tin voucher thành công")
    @GetMapping("/{voucherId}")
    public ResponseEntity<VoucherResponse> getVoucherById(
            @PathVariable("voucherId") Long voucherId
    ) {
        return ResponseEntity.ok(voucherService.getVoucherResponseById(voucherId));
    }
}
