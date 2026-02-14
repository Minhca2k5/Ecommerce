package com.minzetsu.ecommerce.mongo;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditEventService {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventService.class);
    private final AuditEventRepository repository;

    public void archiveFromPayload(String payload) {
        AuditEventDocument doc = new AuditEventDocument();
        doc.setPayload(payload);
        doc.setRequestId(MDC.get("requestId"));
        doc.setCreatedAt(LocalDateTime.now());
        safeSave(doc);
    }

    private void safeSave(AuditEventDocument doc) {
        try {
            repository.save(doc);
        } catch (Exception ex) {
            logger.warn("Failed to persist audit event requestId={} reason={}",
                    doc.getRequestId(),
                    ex.getMessage());
        }
    }
}
