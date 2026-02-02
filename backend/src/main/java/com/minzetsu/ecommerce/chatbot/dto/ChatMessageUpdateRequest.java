package com.minzetsu.ecommerce.chatbot.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatMessageUpdateRequest {
    @NotBlank
    private String content;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
