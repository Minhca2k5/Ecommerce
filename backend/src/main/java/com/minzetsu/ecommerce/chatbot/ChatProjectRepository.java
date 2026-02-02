package com.minzetsu.ecommerce.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatProjectRepository extends JpaRepository<ChatProject, Long> {
    List<ChatProject> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<ChatProject> findByUserIdAndId(Long userId, Long id);
}
