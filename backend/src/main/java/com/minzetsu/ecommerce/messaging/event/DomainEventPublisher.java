package com.minzetsu.ecommerce.messaging.event;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import com.minzetsu.ecommerce.messaging.config.RabbitConfig;

@Service
@RequiredArgsConstructor
public class DomainEventPublisher {
    private final RabbitTemplate rabbitTemplate;

    

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
            case CATEGORY_CREATED, CATEGORY_UPDATED, CATEGORY_DELETED,
                 VOUCHER_CREATED, VOUCHER_UPDATED, VOUCHER_DELETED -> RabbitConfig.ROUTING_CHATBOT;
        };
    }
}




