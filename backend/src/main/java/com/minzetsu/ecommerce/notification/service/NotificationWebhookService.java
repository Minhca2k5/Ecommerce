package com.minzetsu.ecommerce.notification.service;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.common.config.OutboundHttpProperties;
import com.minzetsu.ecommerce.common.utils.OutboundRetryExecutor;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationWebhookService {
    private final RestTemplate restTemplate;
    private final OutboundRetryExecutor retryExecutor;
    private final OutboundHttpProperties properties;
    private final MeterRegistry meterRegistry;
    @Value("${outbound.http.webhook-url:}")
    private String webhookUrl;

    

    public void notifyOrderCreated(Long orderId, Long userId) {
        notifyEvent("ORDER_CREATED", "ORDER", orderId, userId);
    }

    @CircuitBreaker(name = "outboundWebhook", fallbackMethod = "notifyEventFallback")
    public void notifyEvent(String event, String referenceType, Long referenceId, Long userId) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", event);
        payload.put("referenceType", referenceType);
        payload.put("referenceId", referenceId);
        payload.put("userId", userId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        retryExecutor.execute(() -> restTemplate.postForEntity(webhookUrl, entity, Void.class));
    }

    private void notifyEventFallback(
            String event,
            String referenceType,
            Long referenceId,
            Long userId,
            Throwable throwable
    ) {
        meterRegistry.counter("webhook.failures").increment();
    }
}
