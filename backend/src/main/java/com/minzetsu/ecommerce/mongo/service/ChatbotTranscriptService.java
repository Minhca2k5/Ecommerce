package com.minzetsu.ecommerce.mongo.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import com.minzetsu.ecommerce.mongo.document.ChatbotTranscriptDocument;
import com.minzetsu.ecommerce.mongo.repository.ChatbotTranscriptRepository;


@Service
@RequiredArgsConstructor
public class ChatbotTranscriptService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotTranscriptService.class);
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
        } catch (Exception ex) {
            logger.warn("Failed to persist chatbot transcript conversationId={} requestId={} reason={}",
                    doc.getConversationId(),
                    doc.getRequestId(),
                    ex.getMessage());
        }
    }
}




