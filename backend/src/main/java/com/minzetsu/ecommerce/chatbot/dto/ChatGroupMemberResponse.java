package com.minzetsu.ecommerce.chatbot.dto;

public class ChatGroupMemberResponse {
    private Long userId;
    private String username;
    private String fullName;
    private String role;

    public ChatGroupMemberResponse(Long userId, String username, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
}



