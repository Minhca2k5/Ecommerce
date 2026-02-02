package com.minzetsu.ecommerce.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    List<ChatGroup> findByOwnerUserIdOrderByUpdatedAtDesc(Long ownerUserId);
}
