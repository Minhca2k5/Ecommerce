package com.minzetsu.ecommerce.notification.controller.user;

import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import com.minzetsu.ecommerce.notification.dto.request.NotificationCreateRequest;
import com.minzetsu.ecommerce.notification.dto.request.NotificationUpdateRequest;
import com.minzetsu.ecommerce.notification.dto.response.NotificationResponse;
import com.minzetsu.ecommerce.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
@Tag(name = "User - Notifications", description = "Quản lý thông báo của người dùng hiện tại")
public class UserNotificationController {
    private final NotificationService notificationService;

    @Operation(
            summary = "Lấy danh sách thông báo của người dùng hiện tại",
            description = "Trả về tất cả các thông báo liên quan đến người dùng hiện tại (không phân trang)."
    )
    @ApiResponse(responseCode = "200", description = "Thành công - Trả về danh sách thông báo của người dùng hiện tại")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getCurrentUserNotifications() {
        Long userId = getCurrentUserId();
        List<NotificationResponse> notifications = notificationService.getNotificationResponsesByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @Operation(
            summary = "Tạo thông báo cho người dùng hiện tại",
            description = "Tạo một thông báo mới liên quan đến người dùng hiện tại."
    )
    @ApiResponse(responseCode = "200", description = "Thành công - Trả về thông báo đã được tạo")
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotificationForCurrentUser(@Valid @RequestBody NotificationCreateRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok().body(notificationService.createNotificationResponse(request, userId));
    }

    @Operation(
            summary = "Cập nhật trạng thái đã đọc của thông báo",
            description = "Cập nhật trạng thái đã đọc/chưa đọc của một thông báo cụ thể."
    )
    @ApiResponse(responseCode = "200", description = "Thành công - Trạng thái đã đọc của thông báo đã được cập nhật")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> updateNotificationReadStatus(
            @PathVariable("notificationId") Long notificationId,
            @RequestParam("isRead") Boolean isRead
    ) {
        notificationService.updateNotificationReadStatus(notificationId, isRead);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Cập nhật trạng thái ẩn của thông báo",
            description = "Cập nhật trạng thái ẩn/hiện của một thông báo cụ thể."
    )
    @ApiResponse(responseCode = "200", description = "Thành công - Trạng thái ẩn của thông báo đã được cập nhật")
    @PutMapping("/{notificationId}/hidden")
    public ResponseEntity<Void> updateNotificationHiddenStatus(
            @PathVariable("notificationId") Long notificationId,
            @RequestParam("isHidden") Boolean isHidden
    ) {
        notificationService.updateNotificationHiddenStatus(notificationId, isHidden);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Cập nhật trạng thái đã đọc/ẩn cho tất cả thông báo",
            description = "Cập nhật trạng thái đã đọc hoặc ẩn cho tất cả thông báo của người dùng hiện tại."
    )
    @ApiResponse(responseCode = "200", description = "Thành công - Trạng thái thông báo đã được cập nhật")
    @PutMapping("/user")
    public ResponseEntity<Void> updateAllNotificationsStatusForCurrentUser(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) Boolean isHidden
    ) {
        Long userId = getCurrentUserId();
        if (isRead != null) {
            notificationService.updateAllNotificationsReadStatusByUserId(userId, isRead);
        }
        if (isHidden != null) {
            notificationService.updateAllNotificationsHiddenStatusByUserId(userId, isHidden);
        }
        return ResponseEntity.ok().build();
    }
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
