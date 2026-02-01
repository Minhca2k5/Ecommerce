package com.minzetsu.ecommerce.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsConsumer {
    private static final Logger logger = LoggerFactory.getLogger(AnalyticsConsumer.class);

    @RabbitListener(queues = RabbitConfig.QUEUE_ANALYTICS, containerFactory = "rabbitListenerContainerFactory")
    public void handle(DomainEvent event) {
        if (event == null) {
            return;
        }
        logger.info("Analytics event received: type={}, refId={}", event.getType(), event.getReferenceId());
    }
}
