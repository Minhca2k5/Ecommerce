package com.minzetsu.ecommerce.notification.service;

import com.minzetsu.ecommerce.notification.dto.filter.NotificationFilter;
import com.minzetsu.ecommerce.notification.dto.request.NotificationCreateRequest;
import com.minzetsu.ecommerce.notification.dto.request.NotificationUpdateRequest;
import com.minzetsu.ecommerce.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    NotificationResponse createNotificationResponse(NotificationCreateRequest request, Long userId);
    NotificationResponse updateNotificationResponse(NotificationUpdateRequest request, Long id);
    void updateNotificationReadStatus(Long id, Boolean isRead);
    void updateNotificationHiddenStatus(Long id, Boolean isHidden);
    List<NotificationResponse> getNotificationResponsesByUserId(Long userId);
    Page<NotificationResponse> searchNotificationResponses(NotificationFilter filter, Pageable pageable);
    void updateAllNotificationsReadStatusByUserId(Long userId, Boolean isRead);
    void updateAllNotificationsHiddenStatusByUserId(Long userId, Boolean isHidden);
}
