package com.minzetsu.ecommerce.common.audit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.minzetsu.ecommerce.common.audit.entity.AuditLog;
import com.minzetsu.ecommerce.common.audit.repository.AuditLogRepository;
import com.minzetsu.ecommerce.common.audit.telemetry.AuditTelemetryPublisher;



@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditTelemetryPublisher telemetryPublisher;

    @Override
    public void save(AuditLog log) {
        try {
            AuditLog saved = auditLogRepository.save(log);
            telemetryPublisher.publish(saved);
        } catch (Exception ex) {
            AuditLogServiceImpl.log.warn("Failed to persist audit log: {}", ex.getMessage(), ex);
        }
    }
}





