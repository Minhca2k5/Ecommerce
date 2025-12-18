package com.minzetsu.ecommerce.promotion.controller.user;

import com.minzetsu.ecommerce.promotion.dto.filter.BannerFilter;
import com.minzetsu.ecommerce.promotion.dto.response.BannerResponse;
import com.minzetsu.ecommerce.promotion.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/banners")
@RequiredArgsConstructor
@Tag(name = "User - Banners", description = "Quản lý banner dành cho người dùng")
public class BannerController {
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
        Page<BannerResponse> banners = bannerService.searchBanners(filter, pageable, true);
        return ResponseEntity.ok(banners);
    }
}
