package com.minzetsu.ecommerce.chatbot;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatGroupMemberRepository extends JpaRepository<ChatGroupMember, Long> {
    List<ChatGroupMember> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<ChatGroupMember> findByGroupId(Long groupId);
    Optional<ChatGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
    void deleteByGroupId(Long groupId);
}
