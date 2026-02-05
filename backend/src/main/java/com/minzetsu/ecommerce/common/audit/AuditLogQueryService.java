package com.minzetsu.ecommerce.common.audit;

import com.minzetsu.ecommerce.common.audit.dto.filter.AuditLogFilter;
import com.minzetsu.ecommerce.common.audit.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditLogQueryService {
    Page<AuditLogResponse> search(AuditLogFilter filter, Pageable pageable);
}
