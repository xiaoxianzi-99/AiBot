package com.pei.service;

import com.pei.model.Conversation;
import com.pei.model.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Database service for SQLite operations
 * Handles conversation and message persistence
 * @author 帕斯卡的芦苇
 * @date 2025/12/29
 */
public class DatabaseService {
    
    private static final String DB_URL = "jdbc:sqlite:aibot.db";
    private static DatabaseService instance;
    
    private DatabaseService() {
        initializeDatabase();
    }
    
    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }
    
    /**
     * Initialize database and create tables if they don't exist
     */
    private void initializeDatabase() {
        String createConversationsTable = """
            CREATE TABLE IF NOT EXISTS conversations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """;
        
        String createMessagesTable = """
            CREATE TABLE IF NOT EXISTS messages (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                conversation_id INTEGER NOT NULL,
                role TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (conversation_id) REFERENCES conversations(id) ON DELETE CASCADE
            )
        """;
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createConversationsTable);
            stmt.execute(createMessagesTable);
            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Get database connection
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
    
    /**
     * Create a new conversation
     */
    public Conversation createConversation(String title) {
        String sql = "INSERT INTO conversations (title, created_at, updated_at) VALUES (?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            pstmt.setString(1, title);
            pstmt.setString(2, now.toString());
            pstmt.setString(3, now.toString());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    return new Conversation(id, title, now, now);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating conversation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all conversations ordered by updated_at descending
     */
    public List<Conversation> getAllConversations() {
        List<Conversation> conversations = new ArrayList<>();
        String sql = "SELECT id, title, created_at, updated_at FROM conversations ORDER BY updated_at DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Long id = rs.getLong("id");
                String title = rs.getString("title");
                LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
                LocalDateTime updatedAt = LocalDateTime.parse(rs.getString("updated_at"));
                
                conversations.add(new Conversation(id, title, createdAt, updatedAt));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching conversations: " + e.getMessage());
            e.printStackTrace();
        }
        
        return conversations;
    }
    
    /**
     * Get a conversation by ID
     */
    public Conversation getConversation(Long id) {
        String sql = "SELECT id, title, created_at, updated_at FROM conversations WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
                    LocalDateTime updatedAt = LocalDateTime.parse(rs.getString("updated_at"));
                    
                    return new Conversation(id, title, createdAt, updatedAt);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching conversation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Update conversation updated_at timestamp and optionally the title
     */
    public void updateConversation(Long id, String title) {
        String sql = "UPDATE conversations SET updated_at = ?, title = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setString(2, title);
            pstmt.setLong(3, id);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Delete a conversation and all its messages
     */
    public void deleteConversation(Long id) {
        String sql = "DELETE FROM conversations WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting conversation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Save a message to the database
     */
    public Message saveMessage(Long conversationId, String role, String content) {
        String sql = "INSERT INTO messages (conversation_id, role, content, created_at) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            pstmt.setLong(1, conversationId);
            pstmt.setString(2, role);
            pstmt.setString(3, content);
            pstmt.setString(4, now.toString());
            
            pstmt.executeUpdate();
            
            // Update conversation's updated_at timestamp
            updateConversationTimestamp(conversationId);
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long id = generatedKeys.getLong(1);
                    return new Message(id, conversationId, role, content, now);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all messages for a conversation
     */
    public List<Message> getMessagesForConversation(Long conversationId) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT id, conversation_id, role, content, created_at FROM messages WHERE conversation_id = ? ORDER BY created_at ASC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, conversationId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    Long convId = rs.getLong("conversation_id");
                    String role = rs.getString("role");
                    String content = rs.getString("content");
                    LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));
                    
                    messages.add(new Message(id, convId, role, content, createdAt));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching messages: " + e.getMessage());
            e.printStackTrace();
        }
        
        return messages;
    }
    
    /**
     * Update conversation's updated_at timestamp
     */
    private void updateConversationTimestamp(Long conversationId) {
        String sql = "UPDATE conversations SET updated_at = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setLong(2, conversationId);
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating conversation timestamp: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
