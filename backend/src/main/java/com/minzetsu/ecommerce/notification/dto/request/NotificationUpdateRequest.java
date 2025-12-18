package com.minzetsu.ecommerce.notification.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationUpdateRequest {

    private String title;
    private String message;
    private String type;

    private Integer referenceId;
    private String referenceType;

    private Boolean isRead;
    private Boolean isHidden;
}
