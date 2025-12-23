package com.pei.controller;

import com.pei.model.Message;
import com.pei.service.AiService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the chat view
 * @author 帕斯卡的芦苇
 * @date 2025/12/23
 */
public class ChatController {

    @FXML
    private VBox chatContainer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextArea messageInput;

    @FXML
    private Button sendButton;

    private AiService aiService;
    private ExecutorService executorService;

    @FXML
    public void initialize() {
        aiService = new AiService();
        executorService = Executors.newSingleThreadExecutor();
        
        // Auto-scroll to bottom when new messages are added
        chatContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });

        // Send message on Enter key
        messageInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                handleSendMessage();
            }
        });

        // Add welcome message
        addMessageToChat("AI Bot", "你好！我是AI助手，有什么可以帮助你的吗？", false);
    }

    @FXML
    private void handleSendMessage() {
        String userMessage = messageInput.getText().trim();
        
        if (userMessage.isEmpty()) {
            return;
        }

        // Add user message to chat
        addMessageToChat("你", userMessage, true);
        
        // Clear input
        messageInput.clear();
        
        // Disable send button while processing
        sendButton.setDisable(true);
        
        // Send to AI service (async)
        executorService.submit(() -> {
            try {
                String aiResponse = aiService.sendMessage(userMessage);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    addMessageToChat("AI Bot", aiResponse, false);
                    sendButton.setDisable(false);
                });
            } catch (Exception e) {
                // Log the detailed error for debugging
                System.err.println("Error calling AI service: " + e.getMessage());
                e.printStackTrace();
                
                Platform.runLater(() -> {
                    addMessageToChat("系统", "抱歉，处理您的消息时出现问题，请稍后再试。", false);
                    sendButton.setDisable(false);
                });
            }
        });
    }

    /**
     * Add a message to the chat display
     * @param sender The sender name
     * @param content The message content
     * @param isUser Whether this is a user message
     */
    private void addMessageToChat(String sender, String content, boolean isUser) {
        // Create message container
        HBox messageBox = new HBox(10);
        messageBox.setPadding(new Insets(5, 10, 5, 10));
        
        // Create message bubble
        VBox messageBubble = new VBox(5);
        messageBubble.setMaxWidth(500);
        messageBubble.setPadding(new Insets(10));
        
        // Sender label
        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        // Content label
        Label contentLabel = new Label(content);
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");
        
        messageBubble.getChildren().addAll(senderLabel, contentLabel);
        
        // Style based on sender
        if (isUser) {
            messageBubble.getStyleClass().add("user-message");
            messageBox.setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageBubble.getStyleClass().add("ai-message");
            messageBox.setAlignment(Pos.CENTER_LEFT);
        }
        
        messageBox.getChildren().add(messageBubble);
        chatContainer.getChildren().add(messageBox);
    }
    
    /**
     * Cleanup resources when controller is destroyed
     */
    public void cleanup() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
