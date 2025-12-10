package com.minzetsu.ecommerce.user.controller.admin;

import com.minzetsu.ecommerce.user.dto.request.RoleRequest;
import com.minzetsu.ecommerce.user.dto.response.RoleResponse;
import com.minzetsu.ecommerce.user.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Roles", description = "Quản lý vai trò (Role) trong hệ thống")
public class AdminRoleController {

    private final RoleService roleService;

    @Operation(
            summary = "Tạo vai trò mới",
            description = "Tạo một vai trò (role) mới trong hệ thống. Vai trò có thể được gán cho người dùng để quản lý quyền truy cập."
    )
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(
            @Valid @RequestBody RoleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(roleService.createRoleResponse(request));
    }

    @Operation(
            summary = "Lấy danh sách tất cả vai trò",
            description = "Trả về danh sách tất cả các vai trò hiện có trong hệ thống."
    )
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoleResponses());
    }

    @Operation(
            summary = "Lấy thông tin vai trò theo ID",
            description = "Trả về thông tin chi tiết của vai trò dựa trên ID cụ thể."
    )
    @GetMapping("/{roleId}")
    public ResponseEntity<RoleResponse> getRoleById(
            @PathVariable("roleId") Long roleId
    ) {
        return ResponseEntity.ok(roleService.getRoleResponseById(roleId));
    }

    @Operation(
            summary = "Tìm vai trò theo tên",
            description = "Trả về thông tin vai trò dựa trên tên (name) của vai trò."
    )
    @GetMapping("/by-name")
    public ResponseEntity<RoleResponse> getRoleByName(
            @RequestParam("name") String name
    ) {
        return ResponseEntity.ok(roleService.getRoleResponseByName(name));
    }

    @Operation(
            summary = "Xóa vai trò",
            description = "Xóa vai trò dựa trên ID cụ thể. Thường được sử dụng khi vai trò không còn được dùng trong hệ thống."
    )
    @DeleteMapping("/{roleId}")
    public ResponseEntity<Void> deleteRole(
            @PathVariable("roleId") Long roleId
    ) {
        roleService.deleteRoleById(roleId);
        return ResponseEntity.noContent().build();
    }
}
