package com.pei.model;

/**
 * Message model for chat messages
 * @author 帕斯卡的芦苇
 * @date 2025/12/23
 */
public class Message {
    private String sender;
    private String content;
    private long timestamp;
    private boolean isUser;

    public Message(String sender, String content, boolean isUser) {
        this.sender = sender;
        this.content = content;
        this.isUser = isUser;
        this.timestamp = System.currentTimeMillis();
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

    public boolean isUser() {
        return isUser;
    }

    public void setUser(boolean user) {
        isUser = user;
    }
}
