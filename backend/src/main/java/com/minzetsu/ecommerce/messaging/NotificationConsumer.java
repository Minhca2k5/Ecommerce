package com.minzetsu.ecommerce.messaging;

import com.minzetsu.ecommerce.notification.service.NotificationWebhookService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {
    private final NotificationWebhookService notificationWebhookService;

    public NotificationConsumer(NotificationWebhookService notificationWebhookService) {
        this.notificationWebhookService = notificationWebhookService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NOTIFICATION, containerFactory = "rabbitListenerContainerFactory")
    public void handle(DomainEvent event) {
        if (event == null || event.getType() == null || event.getReferenceId() == null) {
            return;
        }
        switch (event.getType()) {
            case ORDER_CREATED -> notificationWebhookService.notifyEvent(
                    "ORDER_CREATED",
                    "ORDER",
                    event.getReferenceId(),
                    event.getUserId()
            );
            case ORDER_STATUS_UPDATED -> notificationWebhookService.notifyEvent(
                    "ORDER_STATUS_UPDATED",
                    "ORDER",
                    event.getReferenceId(),
                    event.getUserId()
            );
            case PAYMENT_CREATED -> notificationWebhookService.notifyEvent(
                    "PAYMENT_CREATED",
                    "PAYMENT",
                    event.getReferenceId(),
                    event.getUserId()
            );
            case PAYMENT_SUCCEEDED -> notificationWebhookService.notifyEvent(
                    "PAYMENT_SUCCEEDED",
                    "PAYMENT",
                    event.getReferenceId(),
                    event.getUserId()
            );
            default -> {
            }
        }
    }
}
