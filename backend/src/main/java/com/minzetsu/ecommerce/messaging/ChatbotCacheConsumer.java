package com.minzetsu.ecommerce.messaging;

import com.minzetsu.ecommerce.chatbot.ChatbotService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ChatbotCacheConsumer {
    private final ChatbotService chatbotService;

    public ChatbotCacheConsumer(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_CHATBOT_CACHE)
    public void handle(DomainEvent event) {
        if (event == null || event.getType() == null) {
            return;
        }
        switch (event.getType()) {
            case PRODUCT_CREATED, PRODUCT_UPDATED, PRODUCT_DELETED -> chatbotService.invalidateProductCache();
            case CATEGORY_CREATED, CATEGORY_UPDATED, CATEGORY_DELETED -> chatbotService.invalidateCategoryCache();
            case VOUCHER_CREATED, VOUCHER_UPDATED, VOUCHER_DELETED -> chatbotService.invalidateVoucherCache();
            default -> {
            }
        }
    }
}
