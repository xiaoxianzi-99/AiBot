package com.pei.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Conversation} model
 */
class ConversationTest {

    @Test
    void defaultConstructor_setsCreatedAtAndUpdatedAtToNow() {
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        Conversation conversation = new Conversation();
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);

        assertNotNull(conversation.getCreatedAt());
        assertNotNull(conversation.getUpdatedAt());
        assertTrue(conversation.getCreatedAt().isAfter(before));
        assertTrue(conversation.getCreatedAt().isBefore(after));
        assertTrue(conversation.getUpdatedAt().isAfter(before));
        assertTrue(conversation.getUpdatedAt().isBefore(after));
    }

    @Test
    void defaultConstructor_leavesIdAndTitleNull() {
        Conversation conversation = new Conversation();
        assertNull(conversation.getId());
        assertNull(conversation.getTitle());
    }

    @Test
    void parameterizedConstructor_setsAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 6, 15, 12, 30);

        Conversation conversation = new Conversation(42L, "Test Chat", createdAt, updatedAt);

        assertEquals(42L, conversation.getId());
        assertEquals("Test Chat", conversation.getTitle());
        assertEquals(createdAt, conversation.getCreatedAt());
        assertEquals(updatedAt, conversation.getUpdatedAt());
    }

    @Test
    void setters_updateFieldsCorrectly() {
        Conversation conversation = new Conversation();

        conversation.setId(99L);
        conversation.setTitle("Updated Title");
        LocalDateTime newCreated = LocalDateTime.of(2024, 3, 1, 8, 0);
        LocalDateTime newUpdated = LocalDateTime.of(2024, 3, 2, 9, 0);
        conversation.setCreatedAt(newCreated);
        conversation.setUpdatedAt(newUpdated);

        assertEquals(99L, conversation.getId());
        assertEquals("Updated Title", conversation.getTitle());
        assertEquals(newCreated, conversation.getCreatedAt());
        assertEquals(newUpdated, conversation.getUpdatedAt());
    }

    @Test
    void toString_containsAllFields() {
        LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2025, 1, 2, 0, 0);
        Conversation conversation = new Conversation(1L, "Hello World", createdAt, updatedAt);

        String result = conversation.toString();

        assertTrue(result.contains("1"));
        assertTrue(result.contains("Hello World"));
        assertTrue(result.contains("createdAt="));
        assertTrue(result.contains("updatedAt="));
    }

    @Test
    void parameterizedConstructor_acceptsNullTitle() {
        Conversation conversation = new Conversation(1L, null, LocalDateTime.now(), LocalDateTime.now());
        assertNull(conversation.getTitle());
    }
}
