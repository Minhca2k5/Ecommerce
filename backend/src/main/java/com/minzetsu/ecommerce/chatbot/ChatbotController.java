package com.minzetsu.ecommerce.chatbot;

import lombok.RequiredArgsConstructor;
import com.minzetsu.ecommerce.chatbot.dto.ChatRequest;
import com.minzetsu.ecommerce.chatbot.dto.ChatResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatMessageResponse;
import com.minzetsu.ecommerce.chatbot.dto.ChatConversationResponse;
import com.minzetsu.ecommerce.common.config.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/users/me/chatbot")
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class ChatbotController {
    private final ChatbotService chatbotService;

    

    @PostMapping
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatbotService.chat(getCurrentUserId(), request);
    }

    @GetMapping("/conversations")
    public java.util.List<ChatConversationResponse> conversations() {
        return chatbotService.listConversations(getCurrentUserId());
    }

    @PostMapping("/conversations")
    public ChatConversationResponse createConversation(@RequestParam(required = false) String title) {
        return chatbotService.createConversation(getCurrentUserId(), title);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public java.util.List<ChatMessageResponse> history(@PathVariable Long conversationId,
                                                       @RequestParam(defaultValue = "20") int limit) {
        return chatbotService.listHistory(getCurrentUserId(), conversationId, limit);
    }

    @DeleteMapping("/conversations/{conversationId}")
    public void clearConversation(@PathVariable Long conversationId) {
        chatbotService.clearHistory(getCurrentUserId(), conversationId);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Unauthenticated");
        }
        return ((CustomUserDetails) authentication.getPrincipal()).getId();
    }
}
