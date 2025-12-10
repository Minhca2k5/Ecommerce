package com.minzetsu.ecommerce.auth.controller;

import com.minzetsu.ecommerce.auth.dto.request.LoginRequest;
import com.minzetsu.ecommerce.auth.dto.request.RegisterRequest;
import com.minzetsu.ecommerce.auth.dto.response.AuthResponse;
import com.minzetsu.ecommerce.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.minzetsu.ecommerce.auth.dto.request.RefreshTokenRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Đăng nhập và đăng ký tài khoản người dùng")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Đăng ký tài khoản mới",
            description = "Tạo tài khoản người dùng mới trong hệ thống. Trả về thông tin xác thực (token, user info) sau khi đăng ký thành công."
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Đăng nhập hệ thống",
            description = "Xác thực người dùng bằng username và password. Trả về token JWT và thông tin người dùng nếu đăng nhập thành công."
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Làm mới Access Token",
            description = "Sử dụng Refresh Token để lấy Access Token mới khi Access Token cũ hết hạn."
    )
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
}
