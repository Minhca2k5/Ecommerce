package com.minzetsu.ecommerce.promotion.controller.pub_user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.promotion.dto.response.UserVoucherResponse;
import com.minzetsu.ecommerce.promotion.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/users/me/vouchers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Vouchers", description = "Quản lý voucher của người dùng")
public class UserVoucherController {
    private final VoucherService voucherService;

    @Operation(summary = "Lấy danh sách voucher của người dùng theo code", description = "Lấy danh sách voucher với khả năng lọc theo mã voucher.")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách voucher thành công")
    @GetMapping
    public ResponseEntity<List<UserVoucherResponse>> getVouchersByCode(@RequestParam String code) {
        Long userId = getCurrentUserId();
        List<UserVoucherResponse> vouchers = voucherService.getVoucherResponsesByCode(code, userId);
        return ResponseEntity.ok(vouchers);
    }

    @Operation(summary = "Lấy danh sách voucher của người dùng theo số tiền đơn hàng tối thiểu", description = "Lấy danh sách voucher với khả năng lọc, phân trang và sắp xếp.")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách voucher thành công")
    @GetMapping("/filter")
    public ResponseEntity<Page<UserVoucherResponse>> getVouchersByMinOrderAmount(
            @RequestParam BigDecimal minOrderAmount,
            Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<UserVoucherResponse> vouchers = voucherService.searchVoucherResponsesByMinOrderTotal(minOrderAmount,
                userId, pageable);
        return ResponseEntity.ok(vouchers);
    }

    @Operation(summary = "Lấy thông tin voucher của người dùng theo ID", description = "Trả về thông tin cơ bản của một voucher (chỉ dữ liệu chính, không có chi tiết liên kết).")
    @ApiResponse(responseCode = "200", description = "Lấy thông tin voucher thành công")
    @GetMapping("/{voucherId}")
    public ResponseEntity<UserVoucherResponse> getVoucherById(
            @PathVariable("voucherId") Long voucherId) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(voucherService.getVoucherResponseById(voucherId, userId));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.minzetsu.ecommerce.common.exception.UnAuthorizedException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
