package com.minzetsu.ecommerce.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class DomainEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    public DomainEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(DomainEventType type, Long referenceId, Long userId, Map<String, Object> payload) {
        DomainEvent event = new DomainEvent(
                UUID.randomUUID().toString(),
                type,
                referenceId,
                userId,
                Instant.now(),
                payload
        );
        String routingKey = resolveRoutingKey(type);
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, routingKey, event);
    }

    private String resolveRoutingKey(DomainEventType type) {
        return switch (type) {
            case PRODUCT_CREATED, PRODUCT_UPDATED, PRODUCT_DELETED -> RabbitConfig.ROUTING_SEARCH;
            case ORDER_CREATED, ORDER_STATUS_UPDATED, PAYMENT_CREATED, PAYMENT_SUCCEEDED -> RabbitConfig.ROUTING_NOTIFICATION;
            case INVENTORY_LOW -> RabbitConfig.ROUTING_ANALYTICS;
        };
    }
}
