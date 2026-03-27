package com.pei.service;

import com.pei.model.Conversation;
import com.pei.model.Message;
import org.junit.jupiter.api.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link DatabaseService}.
 *
 * Each test uses a dedicated in-process SQLite database and cleans up
 * created records via the public delete API, so the tests are self-contained.
 */
class DatabaseServiceTest {

    private DatabaseService db;

    @BeforeEach
    void setUp() throws Exception {
        // Reset the singleton so each test group gets a fresh instance
        // (important if tests run in the same JVM with a different DB state)
        resetSingleton();
        db = DatabaseService.getInstance();
    }

    @AfterEach
    void tearDown() {
        // Nothing extra needed: individual tests delete their own data.
    }

    // ------------------------------------------------------------------ Conversation CRUD

    @Test
    void createConversation_returnsConversationWithId() {
        Conversation conv = db.createConversation("Test Conversation");
        try {
            assertNotNull(conv);
            assertNotNull(conv.getId());
            assertEquals("Test Conversation", conv.getTitle());
            assertNotNull(conv.getCreatedAt());
            assertNotNull(conv.getUpdatedAt());
        } finally {
            if (conv != null) db.deleteConversation(conv.getId());
        }
    }

    @Test
    void getAllConversations_includesNewlyCreatedConversation() {
        Conversation conv = db.createConversation("List Test");
        try {
            assertNotNull(conv);
            List<Conversation> all = db.getAllConversations();
            assertTrue(all.stream().anyMatch(c -> c.getId().equals(conv.getId())));
        } finally {
            if (conv != null) db.deleteConversation(conv.getId());
        }
    }

    @Test
    void getConversation_returnsCorrectConversation() {
        Conversation conv = db.createConversation("GetById Test");
        try {
            assertNotNull(conv);
            Conversation fetched = db.getConversation(conv.getId());
            assertNotNull(fetched);
            assertEquals(conv.getId(), fetched.getId());
            assertEquals("GetById Test", fetched.getTitle());
        } finally {
            if (conv != null) db.deleteConversation(conv.getId());
        }
    }

    @Test
    void getConversation_nonExistentId_returnsNull() {
        Conversation result = db.getConversation(-9999L);
        assertNull(result);
    }

    @Test
    void updateConversation_changesTitle() {
        Conversation conv = db.createConversation("Original Title");
        try {
            assertNotNull(conv);
            db.updateConversation(conv.getId(), "Updated Title");
            Conversation updated = db.getConversation(conv.getId());
            assertNotNull(updated);
            assertEquals("Updated Title", updated.getTitle());
        } finally {
            if (conv != null) db.deleteConversation(conv.getId());
        }
    }

    @Test
    void deleteConversation_removesItFromList() {
        Conversation conv = db.createConversation("To Be Deleted");
        assertNotNull(conv);
        Long id = conv.getId();

        db.deleteConversation(id);

        assertNull(db.getConversation(id));
        List<Conversation> all = db.getAllConversations();
        assertTrue(all.stream().noneMatch(c -> c.getId().equals(id)));
    }

    // ------------------------------------------------------------------ Message CRUD

    @Test
    void saveMessage_returnsMessageWithId() {
        Conversation conv = db.createConversation("Msg Test");
        try {
            assertNotNull(conv);
            Message msg = db.saveMessage(conv.getId(), "user", "Hello!");
            assertNotNull(msg);
            assertNotNull(msg.getId());
            assertEquals(conv.getId(), msg.getConversationId());
            assertEquals("user", msg.getRole());
            assertEquals("Hello!", msg.getContent());
        } finally {
            if (conv != null) db.deleteConversation(conv.getId());
        }
    }

    @Test
    void getMessagesForConversation_returnsAllSavedMessages() {
        Conversation conv = db.createConversation("Multi-Msg Test");
        try {
            assertNotNull(conv);
            db.saveMessage(conv.getId(), "user", "First message");
            db.saveMessage(conv.getId(), "assistant", "Second message");

            List<Message> messages = db.getMessagesForConversation(conv.getId());

            assertEquals(2, messages.size());
            assertEquals("user", messages.get(0).getRole());
            assertEquals("First message", messages.get(0).getContent());
            assertEquals("assistant", messages.get(1).getRole());
            assertEquals("Second message", messages.get(1).getContent());
        } finally {
            if (conv != null) db.deleteConversation(conv.getId());
        }
    }

    @Test
    void getMessagesForConversation_emptyConversation_returnsEmptyList() {
        Conversation conv = db.createConversation("Empty Msgs");
        try {
            assertNotNull(conv);
            List<Message> messages = db.getMessagesForConversation(conv.getId());
            assertNotNull(messages);
            assertTrue(messages.isEmpty());
        } finally {
            if (conv != null) db.deleteConversation(conv.getId());
        }
    }

    @Test
    void deleteConversation_cascadeDeletesMessages() {
        Conversation conv = db.createConversation("Cascade Delete");
        assertNotNull(conv);
        db.saveMessage(conv.getId(), "user", "Will be deleted");
        Long id = conv.getId();

        db.deleteConversation(id);

        List<Message> messages = db.getMessagesForConversation(id);
        assertTrue(messages.isEmpty());
    }

    @Test
    void saveMessage_updatesConversationTimestamp() throws InterruptedException {
        Conversation conv = db.createConversation("Timestamp Test");
        try {
            assertNotNull(conv);
            var updatedAtBefore = db.getConversation(conv.getId()).getUpdatedAt();

            // Small delay to ensure timestamp changes
            Thread.sleep(50);
            db.saveMessage(conv.getId(), "user", "bump timestamp");

            var updatedAtAfter = db.getConversation(conv.getId()).getUpdatedAt();
            assertFalse(updatedAtAfter.isBefore(updatedAtBefore));
        } finally {
            if (conv != null) db.deleteConversation(conv.getId());
        }
    }

    @Test
    void getAllConversations_orderedByUpdatedAtDescending() throws InterruptedException {
        Conversation older = db.createConversation("Older Conv");
        Thread.sleep(50);
        Conversation newer = db.createConversation("Newer Conv");
        try {
            assertNotNull(older);
            assertNotNull(newer);

            List<Conversation> all = db.getAllConversations();
            // Filter to only the two we created
            List<Long> ids = all.stream()
                    .map(Conversation::getId)
                    .filter(id -> id.equals(older.getId()) || id.equals(newer.getId()))
                    .toList();

            assertEquals(2, ids.size());
            // Newer should appear before older in DESC order
            assertEquals(newer.getId(), ids.get(0));
            assertEquals(older.getId(), ids.get(1));
        } finally {
            if (newer != null) db.deleteConversation(newer.getId());
            if (older != null) db.deleteConversation(older.getId());
        }
    }

    // ------------------------------------------------------------------ Singleton

    @Test
    void getInstance_returnsSameInstanceOnMultipleCalls() {
        DatabaseService a = DatabaseService.getInstance();
        DatabaseService b = DatabaseService.getInstance();
        assertSame(a, b);
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Resets the private static {@code instance} field in DatabaseService so that the
     * singleton is re-initialized on the next {@link DatabaseService#getInstance()} call.
     * This ensures each test class setup starts with a clean service state.
     */
    private static void resetSingleton() throws Exception {
        Field field = DatabaseService.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, null);
    }
}
