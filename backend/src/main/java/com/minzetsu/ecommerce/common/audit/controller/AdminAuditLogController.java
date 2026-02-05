package com.minzetsu.ecommerce.common.audit.controller;

import com.minzetsu.ecommerce.common.audit.AuditLogQueryService;
import com.minzetsu.ecommerce.common.audit.dto.filter.AuditLogFilter;
import com.minzetsu.ecommerce.common.audit.dto.response.AuditLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Admin - Audit Logs", description = "Search and filter system audit logs")
public class AdminAuditLogController {

    private final AuditLogQueryService auditLogQueryService;

    @Operation(summary = "Search audit logs with filters")
    @GetMapping
    public ResponseEntity<Page<AuditLogResponse>> search(
            @ModelAttribute AuditLogFilter filter,
            Pageable pageable
    ) {
        return ResponseEntity.ok(auditLogQueryService.search(filter, pageable));
    }
}
