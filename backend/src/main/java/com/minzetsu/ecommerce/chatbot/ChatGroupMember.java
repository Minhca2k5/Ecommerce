package com.minzetsu.ecommerce.chatbot;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chat_group_members")
@Data
public class ChatGroupMember extends BaseEntity {
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 24)
    private String role; // OWNER | MEMBER
}
