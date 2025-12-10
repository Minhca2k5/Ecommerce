package com.minzetsu.ecommerce.notification.controller.admin;

import com.minzetsu.ecommerce.notification.dto.filter.NotificationFilter;
import com.minzetsu.ecommerce.notification.dto.request.NotificationCreateRequest;
import com.minzetsu.ecommerce.notification.dto.request.NotificationUpdateRequest;
import com.minzetsu.ecommerce.notification.dto.response.NotificationResponse;
import com.minzetsu.ecommerce.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Notifications", description = "Quản lý thông báo cho quản trị viên")
public class AdminNotificationController {
    private final NotificationService notificationService;

    @Operation(
            summary = "Cập nhật thông báo của người dùng",
            description = "Cập nhật thông tin của một thông báo cụ thể."
    )
    @ApiResponse(responseCode = "200", description = "Thành công - Trả về thông báo đã được cập nhật")
    @PutMapping("/{notificationId}")
    public ResponseEntity<NotificationResponse> updateNotification(
            @PathVariable("notificationId") Long notificationId,
            @Valid @RequestBody NotificationUpdateRequest request
    ) {
        NotificationResponse updatedNotification = notificationService.updateNotificationResponse(request, notificationId);
        return ResponseEntity.ok(updatedNotification);
    }

    @Operation(
            summary = "Tạo thông báo cho người dùng",
            description = "Tạo một thông báo mới liên quan đến người dùng."
    )
    @ApiResponse(responseCode = "200", description = "Thành công - Trả về thông báo đã được tạo")
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotificationForUser(
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        NotificationResponse createdNotification = notificationService.createNotificationResponse(request, null);
        return ResponseEntity.ok(createdNotification);
    }

    @Operation(
            summary = "Tìm kiếm và lọc thông báo",
            description = "Tìm kiếm và lọc thông báo dựa trên các tiêu chí khác nhau với phân trang."
    )
    @ApiResponse(responseCode = "200", description = "Thành công - Trả về trang thông báo đã lọc")
    @GetMapping("/filter")
    public ResponseEntity<Page<NotificationResponse>> searchNotifications(
            @ModelAttribute NotificationFilter filter,
            Pageable pageable
    ) {
        Page<NotificationResponse> notifications = notificationService.searchNotificationResponses(filter, pageable);
        return ResponseEntity.ok(notifications);
    }
}
