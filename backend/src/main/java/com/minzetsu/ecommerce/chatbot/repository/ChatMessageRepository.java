package com.minzetsu.ecommerce.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.minzetsu.ecommerce.chatbot.entity.ChatMessage;


public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
    Page<ChatMessage> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Modifying
    @Transactional
    void deleteByUserId(Long userId);

    Page<ChatMessage> findByUserIdAndConversationIdOrderByCreatedAtDesc(Long userId, Long conversationId, Pageable pageable);
    Page<ChatMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    @Modifying
    @Transactional
    void deleteByUserIdAndConversationId(Long userId, Long conversationId);

    @Modifying
    @Transactional
    void deleteByConversationId(Long conversationId);
}




