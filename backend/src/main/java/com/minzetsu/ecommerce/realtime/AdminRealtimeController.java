package com.minzetsu.ecommerce.realtime;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin/realtime")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminRealtimeController {
    private final SseEmitterService emitterService;

    

    @GetMapping("/notifications")
    public SseEmitter subscribeNotifications() {
        return emitterService.createAdminEmitter(resolveAdminKey());
    }

    private String resolveAdminKey() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return authentication.getName();
    }
}
