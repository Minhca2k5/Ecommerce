package com.minzetsu.ecommerce.common.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import com.minzetsu.ecommerce.common.audit.repository.AuditLogRepository;
import com.minzetsu.ecommerce.common.audit.config.AuditLogRetentionProperties;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogRetentionCleanupService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogRetentionProperties properties;

    @Scheduled(fixedDelayString = "${audit-log.retention.cleanup-interval-ms:86400000}")
    @Transactional
    public void cleanupExpiredAuditLogs() {
        if (!properties.isEnabled()) {
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(properties.getRetentionDays());
        int batchSize = Math.max(1, properties.getBatchSize());
        int totalDeleted = 0;

        while (true) {
            List<Long> ids = auditLogRepository.findIdsForRetentionCleanup(cutoff, PageRequest.of(0, batchSize));
            if (ids.isEmpty()) {
                break;
            }
            auditLogRepository.deleteAllByIdInBatch(ids);
            totalDeleted += ids.size();
        }

        if (totalDeleted > 0) {
            log.info(
                    "Audit log retention cleanup deleted {} rows older than {} days (cutoff={})",
                    totalDeleted,
                    properties.getRetentionDays(),
                    cutoff
            );
        }
    }
}




