package com.minzetsu.ecommerce.home.controller;

import com.minzetsu.ecommerce.home.dto.HomeResponse;
import com.minzetsu.ecommerce.home.service.HomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "Home", description = "trang chủ ứng dụng")
public class HomeController {

    private final HomeService homeService;

    @Operation(summary = "Lấy dữ liệu trang chủ")
    @GetMapping
    public ResponseEntity<HomeResponse> getHomeData() {
        var data = homeService.getHomeData();
        return ResponseEntity.ok(data);
    }
}
