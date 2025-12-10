package com.minzetsu.ecommerce.promotion.controller.admin;

import com.minzetsu.ecommerce.promotion.dto.filter.VoucherUseFilter;
import com.minzetsu.ecommerce.promotion.dto.response.VoucherUseResponse;
import com.minzetsu.ecommerce.promotion.service.VoucherUseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/voucher-uses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "ADMIN - Voucher Uses", description = "Quản lý việc sử dụng voucher dành cho quản trị viên")
public class AdminVoucherUseController {
    private final VoucherUseService voucherUseService;

    @Operation(
            summary = "Tìm kiếm các bản ghi sử dụng voucher",
            description = "Tìm kiếm các bản ghi sử dụng voucher với khả năng lọc, phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Tìm kiếm các bản ghi sử dụng voucher thành công")
    @GetMapping
    public ResponseEntity<Page<VoucherUseResponse>> searchVoucherUseResponses(
            @ModelAttribute VoucherUseFilter filter,
            Pageable pageable
            ) {
        return ResponseEntity.ok().body(voucherUseService.searchVoucherUseResponses(filter, pageable));
    }

    @Operation(
            summary = "Lấy các bản ghi sử dụng voucher theo userId",
            description = "Lấy các bản ghi sử dụng voucher theo userId với khả năng phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy các bản ghi sử dụng voucher theo userId thành công")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<VoucherUseResponse>> getVoucherUseResponsesByUserId(
            @PathVariable Long userId,
            Pageable pageable
            ) {
        return ResponseEntity.ok().body(voucherUseService.getVoucherUseResponsesByUserId(userId, pageable));
    }

    @Operation(
            summary = "Lấy các bản ghi sử dụng voucher theo orderId",
            description = "Lấy các bản ghi sử dụng voucher theo orderId với khả năng phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy các bản ghi sử dụng voucher theo orderId thành công")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Page<VoucherUseResponse>> getVoucherUseResponseByOrderId(
            @PathVariable Long orderId,
            Pageable pageable
            ) {
        return ResponseEntity.ok().body(voucherUseService.getVoucherUseResponseByOrderId(orderId, pageable));
    }

    @Operation(
            summary = "Lấy các bản ghi sử dụng voucher theo voucherId",
            description = "Lấy các bản ghi sử dụng voucher theo voucherId với khả năng phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy các bản ghi sử dụng voucher theo voucherId thành công")
    @GetMapping("/voucher/{voucherId}")
    public ResponseEntity<Page<VoucherUseResponse>> getVoucherUseResponseByVoucherId(
            @PathVariable Long voucherId,
            Pageable pageable
            ) {
        return ResponseEntity.ok().body(voucherUseService.getVoucherUseResponseByVoucherId(voucherId, pageable));
    }
}
