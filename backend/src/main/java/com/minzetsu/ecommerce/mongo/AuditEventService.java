package com.minzetsu.ecommerce.mongo;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditEventService {

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
        } catch (Exception ignored) {
        }
    }
}
