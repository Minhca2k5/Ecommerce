package com.minzetsu.ecommerce.common.audit;

public interface AuditLogService {
    void save(AuditLog log);
}
