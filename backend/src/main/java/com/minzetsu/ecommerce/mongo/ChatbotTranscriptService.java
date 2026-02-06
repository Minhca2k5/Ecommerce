package com.minzetsu.ecommerce.mongo;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatbotTranscriptService {

    private final ChatbotTranscriptRepository repository;

    public void archiveMessage(Long conversationId, Long userId, String role, String content, String senderName, LocalDateTime createdAt) {
        ChatbotTranscriptDocument doc = new ChatbotTranscriptDocument();
        doc.setConversationId(conversationId);
        doc.setUserId(userId);
        doc.setRole(role);
        doc.setContent(content);
        doc.setSenderName(senderName);
        doc.setRequestId(MDC.get("requestId"));
        doc.setCreatedAt(createdAt == null ? LocalDateTime.now() : createdAt);
        safeSave(doc);
    }

    private void safeSave(ChatbotTranscriptDocument doc) {
        try {
            repository.save(doc);
        } catch (Exception ignored) {
        }
    }
}
