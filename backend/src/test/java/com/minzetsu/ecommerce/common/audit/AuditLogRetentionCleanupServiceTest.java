package com.minzetsu.ecommerce.common.audit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.minzetsu.ecommerce.common.audit.repository.AuditLogRepository;
import com.minzetsu.ecommerce.common.audit.config.AuditLogRetentionProperties;
import com.minzetsu.ecommerce.common.audit.service.AuditLogRetentionCleanupService;


@ExtendWith(MockitoExtension.class)
class AuditLogRetentionCleanupServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditLogRetentionProperties properties;
    private AuditLogRetentionCleanupService cleanupService;

    @BeforeEach
    void setUp() {
        properties = new AuditLogRetentionProperties();
        properties.setEnabled(true);
        properties.setRetentionDays(30);
        properties.setBatchSize(2);

        cleanupService = new AuditLogRetentionCleanupService(auditLogRepository, properties);
    }

    @Test
    void cleanupExpiredAuditLogs_shouldSkipWhenDisabled() {
        properties.setEnabled(false);

        cleanupService.cleanupExpiredAuditLogs();

        verify(auditLogRepository, never()).findIdsForRetentionCleanup(any(LocalDateTime.class), any(Pageable.class));
        verify(auditLogRepository, never()).deleteAllByIdInBatch(any());
    }

    @Test
    void cleanupExpiredAuditLogs_shouldDeleteInBatchesUntilEmpty() {
        when(auditLogRepository.findIdsForRetentionCleanup(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(1L, 2L))
                .thenReturn(List.of(3L))
                .thenReturn(List.of());

        cleanupService.cleanupExpiredAuditLogs();

        verify(auditLogRepository, times(3))
                .findIdsForRetentionCleanup(any(LocalDateTime.class), any(Pageable.class));
        verify(auditLogRepository, times(1)).deleteAllByIdInBatch(List.of(1L, 2L));
        verify(auditLogRepository, times(1)).deleteAllByIdInBatch(List.of(3L));
    }

    @Test
    void cleanupExpiredAuditLogs_shouldStopImmediatelyWhenNoExpiredRows() {
        when(auditLogRepository.findIdsForRetentionCleanup(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());

        cleanupService.cleanupExpiredAuditLogs();

        verify(auditLogRepository, times(1))
                .findIdsForRetentionCleanup(any(LocalDateTime.class), any(Pageable.class));
        verify(auditLogRepository, never()).deleteAllByIdInBatch(any());
    }
}

