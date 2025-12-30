package com.pei.model;

import java.time.LocalDateTime;

/**
 * Message model for chat messages
 * @author 帕斯卡的芦苇
 * @date 2025/12/23
 */
public class Message {
    private Long id;
    private Long conversationId;
    private String role; // "user", "assistant", or "system"
    private String sender;
    private String content;
    private long timestamp;
    private LocalDateTime createdAt;
    private boolean isUser;

    public Message(String sender, String content, boolean isUser) {
        this.sender = sender;
        this.content = content;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
        this.createdAt = LocalDateTime.now();
        this.role = isUser ? "user" : "assistant";
    }

    public Message(Long id, Long conversationId, String role, String content, LocalDateTime createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.role = role;
        this.content = content;
        this.createdAt = createdAt;
        this.timestamp = createdAt != null ? 
            java.sql.Timestamp.valueOf(createdAt).getTime() : System.currentTimeMillis();
        this.isUser = "user".equals(role);
        this.sender = isUser ? "你" : "AI Bot";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
}
