package com.minzetsu.ecommerce.notification.listener;

import com.minzetsu.ecommerce.notification.event.WebhookEvent;
import com.minzetsu.ecommerce.notification.service.NotificationWebhookService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class WebhookEventListener {
    private final NotificationWebhookService webhookService;

    public WebhookEventListener(NotificationWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @Async("appTaskExecutor")
    @EventListener
    public void onWebhookEvent(WebhookEvent event) {
        webhookService.notifyEvent(
                event.getEvent(),
                event.getReferenceType(),
                event.getReferenceId(),
                event.getUserId()
        );
    }
}
