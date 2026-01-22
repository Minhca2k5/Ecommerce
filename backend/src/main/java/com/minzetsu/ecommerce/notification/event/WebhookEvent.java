package com.minzetsu.ecommerce.notification.event;

public class WebhookEvent {
    private final String event;
    private final String referenceType;
    private final Long referenceId;
    private final Long userId;

    public WebhookEvent(String event, String referenceType, Long referenceId, Long userId) {
        this.event = event;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.userId = userId;
    }

    public String getEvent() {
        return event;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public Long getUserId() {
        return userId;
    }
}
