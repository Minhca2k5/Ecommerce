package com.minzetsu.ecommerce.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCreateRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    @NotBlank
    private String type;

    private Integer referenceId;
    private String referenceType;
}
