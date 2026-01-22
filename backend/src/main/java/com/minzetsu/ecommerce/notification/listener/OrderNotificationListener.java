package com.minzetsu.ecommerce.notification.listener;

import com.minzetsu.ecommerce.notification.dto.request.NotificationCreateRequest;
import com.minzetsu.ecommerce.notification.service.NotificationService;
import com.minzetsu.ecommerce.notification.event.WebhookEvent;
import com.minzetsu.ecommerce.order.event.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderNotificationListener {
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    public OrderNotificationListener(
            NotificationService notificationService,
            ApplicationEventPublisher eventPublisher
    ) {
        this.notificationService = notificationService;
        this.eventPublisher = eventPublisher;
    }

    @Async("appTaskExecutor")
    @EventListener
    public void onOrderCreated(OrderCreatedEvent event) {
        NotificationCreateRequest request = NotificationCreateRequest.builder()
                .userId(event.getUserId())
                .title("Order created")
                .message("Your order has been created successfully.")
                .type("ORDER")
                .referenceId(event.getOrderId().intValue())
                .referenceType("ORDER")
                .build();
        notificationService.createNotificationResponse(request, event.getUserId());
        eventPublisher.publishEvent(new WebhookEvent(
                "ORDER_CREATED",
                "ORDER",
                event.getOrderId(),
                event.getUserId()
        ));
    }
}
