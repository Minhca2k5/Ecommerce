package com.minzetsu.ecommerce.messaging;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.search.service.ProductIndexService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchIndexConsumer {
    private final ProductIndexService productIndexService;

    

    @RabbitListener(queues = RabbitConfig.QUEUE_SEARCH, containerFactory = "rabbitListenerContainerFactory")
    public void handle(DomainEvent event) {
        if (event == null || event.getType() == null || event.getReferenceId() == null) {
            return;
        }
        switch (event.getType()) {
            case PRODUCT_CREATED, PRODUCT_UPDATED -> productIndexService.indexProduct(event.getReferenceId());
            case PRODUCT_DELETED -> productIndexService.deleteProduct(event.getReferenceId());
            default -> {
            }
        }
    }
}
