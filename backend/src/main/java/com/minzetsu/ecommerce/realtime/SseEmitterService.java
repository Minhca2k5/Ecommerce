package com.minzetsu.ecommerce.realtime;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterService {
    private final Map<Long, SseEmitter> userEmitters = new ConcurrentHashMap<>();
    private final Map<String, SseEmitter> adminEmitters = new ConcurrentHashMap<>();

    public SseEmitter createUserEmitter(Long userId) {
        SseEmitter emitter = new SseEmitter(0L);
        userEmitters.put(userId, emitter);
        emitter.onCompletion(() -> userEmitters.remove(userId));
        emitter.onTimeout(() -> userEmitters.remove(userId));
        return emitter;
    }

    public SseEmitter createAdminEmitter(String adminKey) {
        SseEmitter emitter = new SseEmitter(0L);
        adminEmitters.put(adminKey, emitter);
        emitter.onCompletion(() -> adminEmitters.remove(adminKey));
        emitter.onTimeout(() -> adminEmitters.remove(adminKey));
        return emitter;
    }

    public void sendToUser(Long userId, String event, Object data) {
        SseEmitter emitter = userEmitters.get(userId);
        if (emitter == null) {
            return;
        }
        try {
            emitter.send(SseEmitter.event()
                    .name(event)
                    .data(data)
                    .id(String.valueOf(Instant.now().toEpochMilli())));
        } catch (IOException ex) {
            userEmitters.remove(userId);
        }
    }

    public void sendToAdmins(String event, Object data) {
        adminEmitters.forEach((key, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name(event)
                        .data(data)
                        .id(String.valueOf(Instant.now().toEpochMilli())));
            } catch (IOException ex) {
                adminEmitters.remove(key);
            }
        });
    }
}
