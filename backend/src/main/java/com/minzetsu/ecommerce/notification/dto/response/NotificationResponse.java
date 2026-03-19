package com.minzetsu.ecommerce.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.minzetsu.ecommerce.common.base.BaseDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse extends BaseDTO {

    private Long userId;
    private String username;
    private String fullName;
    private String title;
    private String message;

    private String type;

    private Integer referenceId;
    private String referenceType;
    private String referenceUrl;

    private Boolean isRead;
    private Boolean isHidden;
}
