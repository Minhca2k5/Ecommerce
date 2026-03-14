package com.minzetsu.ecommerce.chatbot.dto;

import java.time.LocalDateTime;

public class ChatMessageResponse {
    private String role;
    private String content;
    private Long userId;
    private String senderName;
    private LocalDateTime createdAt;

    public ChatMessageResponse() {}

    public ChatMessageResponse(String role, String content, Long userId, String senderName, LocalDateTime createdAt) {
        this.role = role;
        this.content = content;
        this.userId = userId;
        this.senderName = senderName;
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}



