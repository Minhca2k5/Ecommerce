package com.minzetsu.ecommerce.activity.controller.user;

import com.minzetsu.ecommerce.activity.dto.request.RecentViewRequest;
import com.minzetsu.ecommerce.activity.dto.response.RecentViewResponse;
import com.minzetsu.ecommerce.activity.service.RecentViewService;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/recent-views")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Recent Views", description = "Quản lý lịch sử xem sản phẩm của người dùng")
public class UserRecentViewController {

    private final RecentViewService recentViewService;

    @Operation(summary = "Lấy danh sách recent views", description = "Trả về danh sách sản phẩm người dùng đã xem gần đây. Hỗ trợ tìm kiếm theo tên sản phẩm và phân trang.")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách recent views thành công")
    @GetMapping
    public ResponseEntity<Page<RecentViewResponse>> getUserRecentViews(
            @RequestParam(required = false) String productName,
            Pageable pageable) {
        Long userId = getCurrentUserId();
        if (productName != null && !productName.isEmpty()) {
            List<RecentViewResponse> list = recentViewService.getRecentViewsByProductName(productName, userId);
            return ResponseEntity.ok(new org.springframework.data.domain.PageImpl<>(list, pageable, list.size()));
        }
        return ResponseEntity.ok(recentViewService.getRecentViewsByUserId(userId, pageable));
    }

    @Operation(summary = "Thêm sản phẩm vào recent views", description = "Thêm sản phẩm vào danh sách recent views của người dùng. Nếu đã tồn tại record, hệ thống sẽ cập nhật lại thời gian xem.")
    @ApiResponse(responseCode = "200", description = "Thêm hoặc cập nhật recent view thành công")
    @PostMapping
    public ResponseEntity<RecentViewResponse> addUserRecentView(@Valid @RequestBody RecentViewRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(recentViewService.addRecentView(request, userId));
    }

    @Operation(summary = "Xóa một recent view", description = "Xóa một mục recent view theo ID. Người dùng chỉ có thể xóa mục của chính họ.")
    @ApiResponse(responseCode = "200", description = "Xóa recent view thành công")
    @ApiResponse(responseCode = "404", description = "Không tìm thấy recent view")
    @DeleteMapping("/{recentViewId}")
    public ResponseEntity<Void> deleteUserRecentView(@PathVariable Long recentViewId) {
        Long userId = getCurrentUserId();
        recentViewService.deleteRecentView(recentViewId, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Xóa toàn bộ recent views", description = "Xóa toàn bộ lịch sử xem sản phẩm của người dùng.")
    @ApiResponse(responseCode = "200", description = "Xóa toàn bộ recent views thành công")
    @DeleteMapping
    public ResponseEntity<Void> clearUserRecentViews() {
        Long userId = getCurrentUserId();
        recentViewService.deleteAllRecentViewsByUserId(userId);
        return ResponseEntity.ok().build();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.minzetsu.ecommerce.common.exception.UnAuthorizedException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
