package com.minzetsu.ecommerce.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.minzetsu.ecommerce.chatbot.entity.ChatGroupInvite;


public interface ChatGroupInviteRepository extends JpaRepository<ChatGroupInvite, Long> {
    List<ChatGroupInvite> findByInviteeUserIdAndStatusOrderByUpdatedAtDesc(Long inviteeUserId, String status);
    long countByInviteeUserIdAndStatus(Long inviteeUserId, String status);
    long countByInviterUserIdAndStatus(Long inviterUserId, String status);
    Optional<ChatGroupInvite> findByIdAndInviteeUserId(Long id, Long inviteeUserId);
    Optional<ChatGroupInvite> findByGroupIdAndInviteeUserIdAndStatus(Long groupId, Long inviteeUserId, String status);
}




