package com.minzetsu.ecommerce.notification.mapper;

import com.minzetsu.ecommerce.notification.dto.request.NotificationCreateRequest;
import com.minzetsu.ecommerce.notification.dto.request.NotificationUpdateRequest;
import com.minzetsu.ecommerce.notification.dto.response.NotificationResponse;
import com.minzetsu.ecommerce.notification.entity.Notification;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    Notification toEntity(NotificationCreateRequest request);

    void updateEntity(@MappingTarget Notification entity, NotificationUpdateRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "referenceUrl", ignore = true)
    NotificationResponse toResponse(Notification entity);

    List<NotificationResponse> toResponseList(List<Notification> list);
}
