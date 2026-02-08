package com.minzetsu.ecommerce.common.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditTelemetryPublisherTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private AuditTelemetryProperties properties;
    private AuditTelemetryPublisher publisher;

    @BeforeEach
    void setUp() {
        properties = new AuditTelemetryProperties();
        properties.setEnabled(true);
        properties.setChannel("audit-log-events");
        publisher = new AuditTelemetryPublisher(redisTemplate, properties);
    }

    @Test
    void publish_shouldSendTelemetryWhenEnabled() {
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setAction("A|B");
        log.setEntityType("ORDER");
        log.setEntityId(10L);
        log.setSuccess(true);
        log.setCreatedAt(LocalDateTime.of(2026, 2, 8, 21, 5));

        publisher.publish(log);

        verify(redisTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("audit-log-events"), org.mockito.ArgumentMatchers.contains("action=A_B"));
    }

    @Test
    void publish_shouldSkipWhenDisabled() {
        properties.setEnabled(false);
        AuditLog log = new AuditLog();

        publisher.publish(log);

        verify(redisTemplate, never()).convertAndSend(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }
}
