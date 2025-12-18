package com.minzetsu.ecommerce.promotion.controller.admin;

import com.minzetsu.ecommerce.promotion.dto.filter.BannerFilter;
import com.minzetsu.ecommerce.promotion.dto.request.BannerCreateRequest;
import com.minzetsu.ecommerce.promotion.dto.request.BannerUpdateRequest;
import com.minzetsu.ecommerce.promotion.dto.response.BannerResponse;
import com.minzetsu.ecommerce.promotion.service.BannerService;
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
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Banners", description = "Quản lý banner dành cho quản trị viên")
public class AdminBannerController {
    private final BannerService bannerService;

    @Operation(
            summary = "Lấy danh sách banner",
            description = "Lấy danh sách banner với khả năng lọc, phân trang và sắp xếp."
    )
    @ApiResponse(responseCode = "200", description = "Lấy danh sách banner thành công")
    @GetMapping
    public ResponseEntity<Page<BannerResponse>> getBanners(
            @ModelAttribute BannerFilter filter,
            Pageable pageable
            ) {
       Page<BannerResponse> banners = bannerService.searchBanners(filter, pageable, false);
       return ResponseEntity.ok(banners);
    }

    @Operation(
            summary = "Tạo banner mới",
            description = "Tạo một banner mới với các thông tin được cung cấp."
    )
    @ApiResponse(responseCode = "200", description = "Tạo banner thành công")
    @PostMapping
    public ResponseEntity<BannerResponse> createBanner(@Valid @RequestBody BannerCreateRequest request) {
        BannerResponse createdBanner = bannerService.createBanner(request);
        return ResponseEntity.ok(createdBanner);
    }

    @Operation(
            summary = "Cập nhật banner",
            description = "Cập nhật thông tin của một banner hiện có."
    )
    @ApiResponse(responseCode = "200", description = "Cập nhật banner thành công")
    @PutMapping("/{id}")
    public ResponseEntity<BannerResponse> updateBanner(
            @PathVariable("id") Long id,
            @RequestBody BannerUpdateRequest request) {
        BannerResponse updatedBanner = bannerService.updateBanner(id, request);
        return ResponseEntity.ok(updatedBanner);
    }

    @Operation(
            summary = "Xoá banner",
            description = "Xoá một banner khỏi hệ thống."
    )
    @ApiResponse(responseCode = "204", description = "Xoá banner thành công")
    @DeleteMapping("/{bannerId}")
    public ResponseEntity<Void> deleteBanner(@PathVariable("bannerId") Long bannerId) {
        bannerService.deleteBanner(bannerId);
        return ResponseEntity.noContent().build();
    }
}
