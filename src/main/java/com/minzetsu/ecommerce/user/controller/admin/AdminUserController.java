package com.minzetsu.ecommerce.user.controller.admin;

import com.minzetsu.ecommerce.user.dto.filter.UserFilter;
import com.minzetsu.ecommerce.user.dto.request.UserCreateRequest;
import com.minzetsu.ecommerce.user.dto.response.UserResponse;
import com.minzetsu.ecommerce.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Users", description = "Quản lý tài khoản người dùng (User Management)")
public class AdminUserController {

    private final UserService userService;

    @Operation(
            summary = "Tạo người dùng mới",
            description = "Tạo tài khoản người dùng mới trong hệ thống. Admin có thể gán vai trò và thông tin cơ bản."
    )
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUserResponse(request));
    }

    @Operation(
            summary = "Xóa người dùng",
            description = "Xóa người dùng khỏi hệ thống dựa trên ID cụ thể."
    )
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable("userId") Long userId
    ) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Lấy thông tin người dùng theo ID",
            description = "Trả về thông tin cơ bản của người dùng, bao gồm username, email, vai trò và trạng thái hoạt động."
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(userService.getUserResponseById(userId));
    }

    @Operation(
            summary = "Lấy chi tiết đầy đủ người dùng",
            description = "Trả về thông tin chi tiết của người dùng, bao gồm danh sách địa chỉ, đơn hàng, giỏ hàng, v.v."
    )
    @GetMapping("/{userId}/details")
    public ResponseEntity<UserResponse> getFullUserById(
            @PathVariable("userId") Long userId
    ) {
        return ResponseEntity.ok(userService.getFullUserResponseById(userId));
    }

    @Operation(
            summary = "Tìm kiếm người dùng",
            description = "Tìm kiếm và lọc người dùng theo tên, email, vai trò hoặc trạng thái hoạt động. Hỗ trợ phân trang và sắp xếp."
    )
    @GetMapping
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @ModelAttribute UserFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(userService.searchUserResponses(filter, pageable));
    }

    @Operation(
            summary = "Kiểm tra tồn tại username",
            description = "Kiểm tra xem username đã tồn tại trong hệ thống hay chưa."
    )
    @GetMapping("/exists/username")
    public ResponseEntity<Boolean> existsByUsername(
            @RequestParam("username") String username
    ) {
        return ResponseEntity.ok(userService.existsByUsername(username));
    }

    @Operation(
            summary = "Kiểm tra tồn tại email",
            description = "Kiểm tra xem email đã tồn tại trong hệ thống hay chưa."
    )
    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> existsByEmail(
            @RequestParam("email") String email
    ) {
        return ResponseEntity.ok(userService.existsByEmail(email));
    }
}
