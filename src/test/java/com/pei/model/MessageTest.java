package com.pei.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Message} model
 */
class MessageTest {

    // --- Constructor: Message(sender, content, isUser) ---

    @Test
    void userMessageConstructor_setsRoleToUser() {
        Message message = new Message("Alice", "Hello!", true);
        assertEquals("user", message.getRole());
        assertTrue(message.isUser());
    }

    @Test
    void assistantMessageConstructor_setsRoleToAssistant() {
        Message message = new Message("AI Bot", "How can I help?", false);
        assertEquals("assistant", message.getRole());
        assertFalse(message.isUser());
    }

    @Test
    void simpleConstructor_setsSenderAndContent() {
        Message message = new Message("Bob", "Test content", true);
        assertEquals("Bob", message.getSender());
        assertEquals("Test content", message.getContent());
    }

    @Test
    void simpleConstructor_setsTimestampAndCreatedAt() {
        long beforeMs = System.currentTimeMillis();
        Message message = new Message("Bob", "Test", false);
        long afterMs = System.currentTimeMillis();

        assertTrue(message.getTimestamp() >= beforeMs);
        assertTrue(message.getTimestamp() <= afterMs);
        assertNotNull(message.getCreatedAt());
    }

    // --- Constructor: Message(id, conversationId, role, content, createdAt) ---

    @Test
    void fullConstructor_withUserRole_setsIsUserTrue() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 5, 10, 14, 0);
        Message message = new Message(1L, 10L, "user", "Hello", createdAt);

        assertTrue(message.isUser());
        assertEquals("你", message.getSender());
        assertEquals("user", message.getRole());
    }

    @Test
    void fullConstructor_withAssistantRole_setsIsUserFalse() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 5, 10, 14, 1);
        Message message = new Message(2L, 10L, "assistant", "Hi there!", createdAt);

        assertFalse(message.isUser());
        assertEquals("AI Bot", message.getSender());
        assertEquals("assistant", message.getRole());
    }

    @Test
    void fullConstructor_setsAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 6, 20, 9, 30);
        Message message = new Message(5L, 20L, "user", "Content here", createdAt);

        assertEquals(5L, message.getId());
        assertEquals(20L, message.getConversationId());
        assertEquals("Content here", message.getContent());
        assertEquals(createdAt, message.getCreatedAt());
    }

    @Test
    void fullConstructor_withNullCreatedAt_usesCurrentTimeForTimestamp() {
        Message message = new Message(1L, 1L, "user", "text", null);
        // When createdAt is null, timestamp should default to System.currentTimeMillis()
        long now = System.currentTimeMillis();
        assertTrue(message.getTimestamp() <= now);
        assertTrue(message.getTimestamp() > 0);
    }

    @Test
    void fullConstructor_withCreatedAt_derivesTimestampFromIt() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        Message message = new Message(1L, 1L, "user", "text", createdAt);
        long expected = java.sql.Timestamp.valueOf(createdAt).getTime();
        assertEquals(expected, message.getTimestamp());
    }

    // --- Setters ---

    @Test
    void setters_updateAllFields() {
        Message message = new Message("user", "original", true);

        message.setId(100L);
        message.setConversationId(200L);
        message.setRole("assistant");
        message.setSender("NewSender");
        message.setContent("new content");
        message.setTimestamp(999L);
        LocalDateTime newCreatedAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        message.setCreatedAt(newCreatedAt);
        message.setUser(false);

        assertEquals(100L, message.getId());
        assertEquals(200L, message.getConversationId());
        assertEquals("assistant", message.getRole());
        assertEquals("NewSender", message.getSender());
        assertEquals("new content", message.getContent());
        assertEquals(999L, message.getTimestamp());
        assertEquals(newCreatedAt, message.getCreatedAt());
        assertFalse(message.isUser());
    }
}
