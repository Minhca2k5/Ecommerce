package com.minzetsu.ecommerce.chatbot.dto;

import java.time.LocalDateTime;

public class ChatGroupResponse {
    private Long id;
    private String name;
    private Long ownerUserId;
    private String role;
    private LocalDateTime updatedAt;

    public ChatGroupResponse() {}

    public ChatGroupResponse(Long id, String name, Long ownerUserId, String role, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.ownerUserId = ownerUserId;
        this.role = role;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
