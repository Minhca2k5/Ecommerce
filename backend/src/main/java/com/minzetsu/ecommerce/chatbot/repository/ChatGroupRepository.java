package com.minzetsu.ecommerce.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import com.minzetsu.ecommerce.chatbot.entity.ChatGroup;


public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    List<ChatGroup> findByOwnerUserIdOrderByUpdatedAtDesc(Long ownerUserId);
}





