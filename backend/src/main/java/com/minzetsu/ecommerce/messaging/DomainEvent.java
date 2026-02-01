package com.minzetsu.ecommerce.messaging;

import java.time.Instant;
import java.util.Map;

public class DomainEvent {
    private String eventId;
    private DomainEventType type;
    private Long referenceId;
    private Long userId;
    private Instant createdAt;
    private Map<String, Object> payload;

    public DomainEvent() {
    }

    public DomainEvent(
            String eventId,
            DomainEventType type,
            Long referenceId,
            Long userId,
            Instant createdAt,
            Map<String, Object> payload
    ) {
        this.eventId = eventId;
        this.type = type;
        this.referenceId = referenceId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.payload = payload;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public DomainEventType getType() {
        return type;
    }

    public void setType(DomainEventType type) {
        this.type = type;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Long referenceId) {
        this.referenceId = referenceId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}
