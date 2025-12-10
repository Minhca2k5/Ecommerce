package com.minzetsu.ecommerce.search.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.search.dto.request.SearchLogRequest;
import com.minzetsu.ecommerce.search.dto.response.SearchLogResponse;
import com.minzetsu.ecommerce.search.service.SearchLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/search-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Search Logs", description = "Quản lý lịch sử tìm kiếm của người dùng")
public class UserSearchLogController {

    private final SearchLogService searchLogService;

    @Operation(
            summary = "Lấy danh sách search logs của người dùng",
            description = "Trả về danh sách các từ khóa tìm kiếm mà người dùng đã sử dụng. Hỗ trợ tìm kiếm theo từ khóa và phân trang."
    )
    @ApiResponse(responseCode = "200", description = "Lấy danh sách search logs thành công")
    @GetMapping
    public ResponseEntity<List<SearchLogResponse>> getUserSearchLogs(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        Long userId = getCurrentUserId();
        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(searchLogService.getSearchLogsByKeyword(keyword, userId));
        }
        return ResponseEntity.ok(searchLogService.getSearchLogsByUserId(userId, pageable));
    }

    @Operation(
            summary = "Thêm search log mới",
            description = "Thêm một từ khóa tìm kiếm mới vào lịch sử tìm kiếm của người dùng."
    )
    @ApiResponse(responseCode = "200", description = "Thêm search log thành công")
    @PostMapping
    public ResponseEntity<SearchLogResponse> addUserSearchLog(@Valid @RequestBody SearchLogRequest request) {
        Long userId = getCurrentUserId();
        SearchLogResponse response = searchLogService.addSearchLog(request, userId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Xóa tất cả search logs của người dùng",
            description = "Xóa toàn bộ lịch sử tìm kiếm của người dùng."
    )
    @ApiResponse(responseCode = "204", description = "Xóa tất cả search logs thành công")
    @DeleteMapping
    public ResponseEntity<Void> deleteAllUserSearchLogs() {
        Long userId = getCurrentUserId();
        searchLogService.deleteAllSearchLogsByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Xóa một search log",
            description = "Xóa một mục lịch sử tìm kiếm theo ID. Người dùng chỉ có thể xóa mục của chính họ."
    )
    @ApiResponse(responseCode = "204", description = "Xóa search log thành công")
    @DeleteMapping("/{searchLogId}")
    public ResponseEntity<Void> deleteUserSearchLog(@PathVariable Long searchLogId) {
        Long userId = getCurrentUserId();
        searchLogService.deleteSearchLog(searchLogId, userId);
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
