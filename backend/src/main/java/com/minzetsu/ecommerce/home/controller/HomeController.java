package com.minzetsu.ecommerce.home.controller;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.home.dto.request.BannerClickRequest;
import com.minzetsu.ecommerce.home.dto.response.HomeResponse;
import com.minzetsu.ecommerce.home.service.HomeService;
import com.minzetsu.ecommerce.mongo.service.ClickstreamEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "Trang chủ ứng dụng")
public class HomeController {

    private final HomeService homeService;
    private final ClickstreamEventService clickstreamEventService;

    @Operation(summary = "Lấy dữ liệu trang chủ")
    @GetMapping
    public ResponseEntity<HomeResponse> getHomeData() {
        var data = homeService.getHomeData();
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Track homepage banner click")
    @PostMapping("/banner-click")
    public ResponseEntity<Void> trackBannerClick(@Valid @RequestBody BannerClickRequest request) {
        clickstreamEventService.recordBannerClick(
                getCurrentUserIdOrNull(),
                request.getGuestId(),
                request.getBannerKey(),
                request.getPlacement(),
                request.getTargetPath());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private Long getCurrentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}
