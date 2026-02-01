package com.minzetsu.ecommerce.realtime;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/users/me/realtime")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class UserRealtimeController {
    private final SseEmitterService emitterService;

    

    @GetMapping("/orders")
    public SseEmitter subscribeOrders() {
        Long userId = getCurrentUserId();
        return emitterService.createUserEmitter(userId);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
