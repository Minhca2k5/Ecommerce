package com.minzetsu.ecommerce.common.audit.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String action;
    private String entityType;
    private Long entityId;
    private Boolean success;
    private String errorMessage;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}



