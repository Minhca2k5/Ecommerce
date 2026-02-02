package com.minzetsu.ecommerce.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    List<ChatConversation> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<ChatConversation> findByUserIdAndId(Long userId, Long id);
    Optional<ChatConversation> findByUserIdAndTitle(Long userId, String title);
    List<ChatConversation> findByUserIdAndProjectIdOrderByUpdatedAtDesc(Long userId, Long projectId);
    List<ChatConversation> findByGroupIdOrderByUpdatedAtDesc(Long groupId);
    List<ChatConversation> findByUserIdAndProjectIdIsNullAndGroupIdIsNullOrderByUpdatedAtDesc(Long userId);
}
