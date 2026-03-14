package com.minzetsu.ecommerce.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.minzetsu.ecommerce.chatbot.entity.ChatProject;


public interface ChatProjectRepository extends JpaRepository<ChatProject, Long> {
    List<ChatProject> findByUserIdOrderByUpdatedAtDesc(Long userId);
    Optional<ChatProject> findByUserIdAndId(Long userId, Long id);
}




