package com.minzetsu.ecommerce.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditTelemetryPublisher {

    private final StringRedisTemplate redisTemplate;
    private final AuditTelemetryProperties properties;

    public void publish(AuditLog auditLog) {
        if (!properties.isEnabled()) {
            return;
        }
        try {
            String payload = String.format(
                    "id=%d|action=%s|entity=%s|entityId=%s|success=%s|createdAt=%s",
                    auditLog.getId(),
                    safe(auditLog.getAction()),
                    safe(auditLog.getEntityType()),
                    auditLog.getEntityId(),
                    auditLog.getSuccess(),
                    auditLog.getCreatedAt()
            );
            redisTemplate.convertAndSend(properties.getChannel(), payload);
        } catch (Exception ex) {
            log.warn("Failed to publish audit telemetry event: {}", ex.getMessage());
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("|", "_");
    }
}
