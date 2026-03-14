package com.minzetsu.ecommerce.common.audit.service;

import com.minzetsu.ecommerce.common.audit.entity.AuditLog;


public interface AuditLogService {
    void save(AuditLog log);
}




