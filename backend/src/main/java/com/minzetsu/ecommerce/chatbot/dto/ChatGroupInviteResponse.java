package com.minzetsu.ecommerce.chatbot.dto;

import java.time.LocalDateTime;

public class ChatGroupInviteResponse {
    private Long id;
    private Long groupId;
    private Long inviterUserId;
    private Long inviteeUserId;
    private String status;
    private LocalDateTime updatedAt;

    public ChatGroupInviteResponse() {}

    public ChatGroupInviteResponse(Long id, Long groupId, Long inviterUserId, Long inviteeUserId, String status, LocalDateTime updatedAt) {
        this.id = id;
        this.groupId = groupId;
        this.inviterUserId = inviterUserId;
        this.inviteeUserId = inviteeUserId;
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }
    public Long getInviterUserId() { return inviterUserId; }
    public void setInviterUserId(Long inviterUserId) { this.inviterUserId = inviterUserId; }
    public Long getInviteeUserId() { return inviteeUserId; }
    public void setInviteeUserId(Long inviteeUserId) { this.inviteeUserId = inviteeUserId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
