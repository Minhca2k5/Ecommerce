package com.minzetsu.ecommerce.user.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.user.dto.request.PasswordRequest;
import com.minzetsu.ecommerce.user.dto.request.UserUpdateRequest;
import com.minzetsu.ecommerce.user.dto.response.UserResponse;
import com.minzetsu.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Profile", description = "Quản lý thông tin cá nhân của người dùng (User Profile Management)")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Lấy thông tin cá nhân cơ bản",
            description = "Trả về thông tin cơ bản của người dùng hiện tại như tên, email, vai trò, trạng thái, v.v."
    )
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getProfile() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.getUserResponseById(userId));
    }

    @Operation(
            summary = "Lấy thông tin cá nhân chi tiết",
            description = "Trả về thông tin chi tiết của người dùng hiện tại, bao gồm địa chỉ, đơn hàng, giỏ hàng, v.v."
    )
    @GetMapping("/me/details")
    public ResponseEntity<UserResponse> getFullProfile() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.getFullUserResponseById(userId));
    }

    @Operation(
            summary = "Cập nhật thông tin cá nhân",
            description = "Cho phép người dùng cập nhật thông tin hồ sơ như tên, số điện thoại, địa chỉ email (nếu được phép)."
    )
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(
            @Valid @RequestBody UserUpdateRequest request
    ) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.updateUserResponse(request, userId));
    }

    @Operation(
            summary = "Thay đổi mật khẩu",
            description = "Cho phép người dùng thay đổi mật khẩu bằng cách cung cấp mật khẩu hiện tại và mật khẩu mới."
    )
    @PatchMapping("/me/password")
    public ResponseEntity<UserResponse> changePassword(
            @Valid @RequestBody PasswordRequest request
    ) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(userService.changeUserPassword(userId, request));
    }

    @Operation(
            summary = "Xóa tài khoản người dùng",
            description = "Xóa vĩnh viễn tài khoản của người dùng hiện tại khỏi hệ thống (sau khi xác thực)."
    )
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        Long userId = getCurrentUserId();
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
