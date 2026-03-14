package com.minzetsu.ecommerce.common.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import com.minzetsu.ecommerce.common.audit.config.AuditTelemetryProperties;
import com.minzetsu.ecommerce.common.audit.telemetry.AuditTelemetryPublisher;
import com.minzetsu.ecommerce.common.audit.entity.AuditLog;


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

        verify(redisTemplate).convertAndSend(eq("audit-log-events"), contains("action=A_B"));
    }

    @Test
    void publish_shouldSkipWhenDisabled() {
        properties.setEnabled(false);
        AuditLog log = new AuditLog();

        publisher.publish(log);

        verify(redisTemplate, never()).convertAndSend(anyString(), anyString());
    }

    @Test
    void publish_shouldNotThrowWhenRedisPublishFails() {
        AuditLog log = new AuditLog();
        log.setId(2L);
        log.setAction("ORDER_UPDATED");
        log.setEntityType("ORDER");
        log.setEntityId(11L);
        log.setSuccess(true);
        log.setCreatedAt(LocalDateTime.of(2026, 2, 8, 21, 6));

        doThrow(new RuntimeException("redis down"))
                .when(redisTemplate)
                .convertAndSend(eq("audit-log-events"), anyString());

        assertThatCode(() -> publisher.publish(log)).doesNotThrowAnyException();
    }
}

