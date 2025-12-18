package com.minzetsu.ecommerce.promotion.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.promotion.dto.response.VoucherUseResponse;
import com.minzetsu.ecommerce.promotion.service.VoucherUseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/voucher-uses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Voucher Uses", description = "Quản lý việc sử dụng voucher dành cho người dùng")
public class UserVoucherUseController {
    private final VoucherUseService voucherUseService;

    @Operation(
            summary = "Lấy các bản ghi sử dụng voucher của người dùng hiện tại",
            description = "Lấy các bản ghi sử dụng voucher của người dùng hiện tại với khả năng phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy các bản ghi sử dụng voucher của người dùng hiện tại thành công")
    @GetMapping("/user/me")
    public ResponseEntity<Page<VoucherUseResponse>> getVoucherUseResponses(Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<VoucherUseResponse> voucherUses = voucherUseService.getVoucherUseResponsesByUserId(userId, pageable);
        return ResponseEntity.ok(voucherUses);
    }

    @Operation(
            summary = "Lấy các bản ghi sử dụng voucher theo orderId của người dùng hiện tại",
            description = "Lấy các bản ghi sử dụng voucher theo orderId của người dùng hiện tại với khả năng phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy các bản ghi sử dụng voucher theo orderId của người dùng hiện tại thành công")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Page<VoucherUseResponse>> getVoucherUseResponsesByOrderId(
            @PathVariable Long orderId,
            Pageable pageable
    ) {
        Long userId = getCurrentUserId();
        Page<VoucherUseResponse> voucherUses = voucherUseService.getVoucherUseResponseByOrderIdAndUserId(orderId, userId, pageable);
        return ResponseEntity.ok(voucherUses);
    }

    @Operation(
            summary = "Lấy các bản ghi sử dụng voucher theo voucherId của người dùng hiện tại",
            description = "Lấy các bản ghi sử dụng voucher theo voucherId của người dùng hiện tại với khả năng phân trang và sắp xếp.")
    @ApiResponse(responseCode = "200", description = "Lấy các bản ghi sử dụng voucher theo voucherId của người dùng hiện tại thành công")
    @GetMapping("/voucher/{voucherId}")
    public ResponseEntity<Page<VoucherUseResponse>> getVoucherUseResponsesByVoucherId(
            @PathVariable Long voucherId,
            Pageable pageable
    ) {
        Long userId = getCurrentUserId();
        Page<VoucherUseResponse> voucherUses = voucherUseService.getVoucherUseResponseByVoucherIdAndUserId(voucherId, userId, pageable);
        return ResponseEntity.ok(voucherUses);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
