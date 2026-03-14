package com.minzetsu.ecommerce.realtime.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatbotRealtimeService {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long conversationId, Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        String key = buildKey(conversationId, userId, Instant.now().toEpochMilli());
        emitters.put(key, emitter);
        emitter.onCompletion(() -> emitters.remove(key));
        emitter.onTimeout(() -> emitters.remove(key));
        return emitter;
    }

    public void publishToConversation(Long conversationId, String event, Object data) {
        String prefix = conversationId + ":";
        emitters.forEach((key, emitter) -> {
            if (!key.startsWith(prefix)) {
                return;
            }
            try {
                emitter.send(SseEmitter.event()
                        .name(event)
                        .data(data)
                        .id(String.valueOf(Instant.now().toEpochMilli())));
            } catch (IOException ex) {
                emitters.remove(key);
            }
        });
    }

    private String buildKey(Long conversationId, Long userId, long nonce) {
        return conversationId + ":" + userId + ":" + nonce;
    }
}



