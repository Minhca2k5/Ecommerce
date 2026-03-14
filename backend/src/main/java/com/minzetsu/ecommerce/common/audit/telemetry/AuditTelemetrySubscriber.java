package com.minzetsu.ecommerce.common.audit.telemetry;

import com.minzetsu.ecommerce.mongo.service.AuditEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import com.minzetsu.ecommerce.common.audit.config.AuditTelemetryProperties;


@Component
@RequiredArgsConstructor
@Slf4j
public class AuditTelemetrySubscriber implements MessageListener {
    private final AuditEventService auditEventService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        log.info("Audit telemetry event received: {}", payload);
        auditEventService.archiveFromPayload(payload);
    }
}





