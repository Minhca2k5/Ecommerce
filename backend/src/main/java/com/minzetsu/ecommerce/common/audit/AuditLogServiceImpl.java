package com.minzetsu.ecommerce.common.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public void save(AuditLog log) {
        try {
            auditLogRepository.save(log);
        } catch (Exception ex) {
            AuditLogServiceImpl.log.warn("Failed to persist audit log: {}", ex.getMessage(), ex);
        }
    }
}
