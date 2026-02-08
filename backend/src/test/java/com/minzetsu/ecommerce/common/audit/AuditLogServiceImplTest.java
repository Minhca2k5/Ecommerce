package com.minzetsu.ecommerce.common.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditTelemetryPublisher telemetryPublisher;

    private AuditLogServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuditLogServiceImpl(auditLogRepository, telemetryPublisher);
    }

    @Test
    void save_shouldPersistAndPublishTelemetry() {
        AuditLog log = new AuditLog();
        log.setAction("ORDER_CREATED");
        when(auditLogRepository.save(log)).thenReturn(log);

        service.save(log);

        verify(auditLogRepository).save(log);
        verify(telemetryPublisher).publish(log);
    }

    @Test
    void save_shouldSwallowPersistenceExceptions() {
        AuditLog log = new AuditLog();
        when(auditLogRepository.save(log)).thenThrow(new RuntimeException("db down"));

        service.save(log);

        verify(auditLogRepository).save(log);
        verify(telemetryPublisher, never()).publish(log);
    }

    @Test
    void save_shouldSwallowTelemetryPublishExceptions() {
        AuditLog log = new AuditLog();
        log.setAction("ORDER_UPDATED");
        when(auditLogRepository.save(log)).thenReturn(log);
        org.mockito.Mockito.doThrow(new RuntimeException("redis down"))
                .when(telemetryPublisher)
                .publish(log);

        assertThatCode(() -> service.save(log)).doesNotThrowAnyException();

        verify(auditLogRepository).save(log);
        verify(telemetryPublisher).publish(log);
    }

    @Test
    void save_shouldPublishPersistedInstanceFromRepository() {
        AuditLog request = new AuditLog();
        request.setAction("ORDER_CREATED");

        AuditLog persisted = new AuditLog();
        persisted.setAction("ORDER_CREATED");
        persisted.setId(99L);

        when(auditLogRepository.save(request)).thenReturn(persisted);

        service.save(request);

        verify(telemetryPublisher).publish(persisted);
        verify(telemetryPublisher, never()).publish(request);
    }
}
