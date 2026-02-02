package com.minzetsu.ecommerce.chatbot;

import com.minzetsu.ecommerce.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chat_conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversation extends BaseEntity {
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "group_id")
    private Long groupId;
}
