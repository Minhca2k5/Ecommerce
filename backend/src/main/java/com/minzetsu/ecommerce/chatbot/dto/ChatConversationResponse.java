package com.minzetsu.ecommerce.chatbot.dto;

import java.time.LocalDateTime;

public class ChatConversationResponse {
    private Long id;
    private String title;
    private LocalDateTime updatedAt;

    public ChatConversationResponse() {}

    public ChatConversationResponse(Long id, String title, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}



