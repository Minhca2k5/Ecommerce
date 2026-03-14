package com.minzetsu.ecommerce.chatbot.entity;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chat_group_invites")
@Data
public class ChatGroupInvite extends BaseEntity {
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "inviter_user_id", nullable = false)
    private Long inviterUserId;

    @Column(name = "invitee_user_id", nullable = false)
    private Long inviteeUserId;

    @Column(nullable = false, length = 24)
    private String status; // PENDING, ACCEPTED, DECLINED
}



