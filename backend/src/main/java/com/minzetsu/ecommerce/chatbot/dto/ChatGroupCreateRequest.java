package com.minzetsu.ecommerce.chatbot.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatGroupCreateRequest {
    @NotBlank
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
