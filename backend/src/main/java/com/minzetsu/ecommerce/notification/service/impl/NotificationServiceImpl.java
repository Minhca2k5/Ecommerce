package com.minzetsu.ecommerce.notification.service.impl;

import com.minzetsu.ecommerce.common.audit.AuditAction;
import com.minzetsu.ecommerce.common.utils.PageableUtils;
import com.minzetsu.ecommerce.notification.dto.filter.NotificationFilter;
import com.minzetsu.ecommerce.notification.dto.request.NotificationCreateRequest;
import com.minzetsu.ecommerce.notification.dto.request.NotificationUpdateRequest;
import com.minzetsu.ecommerce.notification.dto.response.NotificationResponse;
import com.minzetsu.ecommerce.notification.entity.Notification;
import com.minzetsu.ecommerce.notification.entity.NotificationType;
import com.minzetsu.ecommerce.notification.mapper.NotificationMapper;
import com.minzetsu.ecommerce.notification.repository.NotificationRepository;
import com.minzetsu.ecommerce.notification.repository.NotificationSpecification;
import com.minzetsu.ecommerce.notification.service.NotificationService;
import com.minzetsu.ecommerce.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final UserService userService;
    @Value("${admin-endpoint}")
    private String adminEndpoint;
    @Value("${user-endpoint}")
    private String userEndpoint;
    @Value("${public-endpoint}")
    private String publicEndpoint;

    void existsById(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification not found with id: " + id);
        }
    }

    private static final Map<String, String> ENDPOINT_MAPPING = Map.of(
            "ORDER", "orders",
            "PAYMENT", "orders",
            "REVIEW", "reviews",
            "VOUCHER", "vouchers",
            "PRODUCT", "products",
            "CATEGORY", "categories"
    );

    private final List<NotificationType> publicKeys = new ArrayList<>(
            List.of(
                    NotificationType.PRODUCT,
                    NotificationType.CATEGORY
            )
    );

    private String resolveEndpoint(NotificationType type, String referenceType) {
        if (referenceType == null || referenceType.isBlank()) {
            return ENDPOINT_MAPPING.getOrDefault(type.name(), "");
        }
        return ENDPOINT_MAPPING.getOrDefault(referenceType.toUpperCase(), referenceType.toLowerCase().trim() + "s");
    }

    private NotificationResponse toResponse(Notification notification) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        String baseEndpoint;
        if (publicKeys.contains(notification.getType())) {
            baseEndpoint = publicEndpoint;
        } else if (isAdmin) {
            baseEndpoint = adminEndpoint;
        } else {
            baseEndpoint = userEndpoint;
        }
        NotificationType type = notification.getType();
        String endpoint = resolveEndpoint(type, notification.getReferenceType());
        String url = "";
        if (!endpoint.isBlank() && notification.getReferenceId() != null) {
            url = String.format("%s/%s/%d", baseEndpoint, endpoint, notification.getReferenceId());
        }

        NotificationResponse response = notificationMapper.toResponse(notification);
        response.setReferenceUrl(url);
        return response;
    }

    @Override
    @Transactional
    @AuditAction(action = "NOTIFICATION_CREATED", entityType = "NOTIFICATION")
    public NotificationResponse createNotificationResponse(NotificationCreateRequest request, Long userId) {
        if (userId != null) {
            request.setUserId(userId);
        }
        Notification notification = notificationMapper.toEntity(request);
        notification.setUser(userService.getUserById(userId));
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    @AuditAction(action = "NOTIFICATION_UPDATED", entityType = "NOTIFICATION", idParamIndex = 1)
    public NotificationResponse updateNotificationResponse(NotificationUpdateRequest request, Long id) {
        Notification existingNotification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + id));
        notificationMapper.updateEntity(existingNotification, request);
        return toResponse(notificationRepository.save(existingNotification));
    }

    @Override
    @Transactional
    @AuditAction(action = "NOTIFICATION_READ_UPDATED", entityType = "NOTIFICATION", idParamIndex = 0)
    public void updateNotificationReadStatus(Long id, Boolean isRead) {
        existsById(id);
        notificationRepository.updateIsReadById(id, isRead);
    }

    @Override
    @Transactional
    @AuditAction(action = "NOTIFICATION_HIDDEN_UPDATED", entityType = "NOTIFICATION", idParamIndex = 0)
    public void updateNotificationHiddenStatus(Long id, Boolean isHidden) {
        existsById(id);
        notificationRepository.updateIsHiddenById(id, isHidden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationResponsesByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> searchNotificationResponses(NotificationFilter filter, Pageable pageable) {
        return PageableUtils.search(
                filter,
                pageable,
                notificationRepository,
                NotificationSpecification.filter(filter),
                this::toResponse
        );
    }

    @Override
    @Transactional
    @AuditAction(action = "NOTIFICATION_READ_ALL_UPDATED", entityType = "NOTIFICATION")
    public void updateAllNotificationsReadStatusByUserId(Long userId, Boolean isRead) {
        notificationRepository.updateIsReadByUserId(userId, isRead);
    }

    @Override
    @Transactional
    @AuditAction(action = "NOTIFICATION_HIDDEN_ALL_UPDATED", entityType = "NOTIFICATION")
    public void updateAllNotificationsHiddenStatusByUserId(Long userId, Boolean isHidden) {
        notificationRepository.updateIsHiddenByUserId(userId, isHidden);
    }
}
