package com.minzetsu.ecommerce.realtime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/public/realtime")
public class PublicRealtimeController {
    private final SseEmitterService emitterService;

    public PublicRealtimeController(SseEmitterService emitterService) {
        this.emitterService = emitterService;
    }

    @GetMapping("/new-products")
    public SseEmitter subscribeNewProducts() {
        return emitterService.createAdminEmitter("public-new-products");
    }
}
