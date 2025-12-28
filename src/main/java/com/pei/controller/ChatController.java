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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Controller for the chat view
 * @author 帕斯卡的芦苇
 * @date 2025/12/23
 */
public class ChatController {

    // Supported file extensions for upload
    private static final String[] SUPPORTED_TEXT_EXTENSIONS = {
        "*.txt", "*.java", "*.py", "*.js", "*.json", "*.xml", "*.md", "*.csv"
    };

    @FXML
    private VBox chatContainer;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private TextArea messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private Button uploadButton;

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
        addMessageToChat("AI Bot", "你好！我是AI助手，有什么可以帮助你的吗？\n\n💡 提示：你可以直接和我对话，也可以点击 '📎 上传文件' 按钮上传文件让我帮你分析。", false);
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

    @FXML
    private void handleUploadFile() {
        // Create file chooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择文件供 AI 分析");
        
        // Add file filters for supported text formats
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("所有支持的文件", SUPPORTED_TEXT_EXTENSIONS),
            new FileChooser.ExtensionFilter("文本文件", "*.txt"),
            new FileChooser.ExtensionFilter("Java文件", "*.java"),
            new FileChooser.ExtensionFilter("Python文件", "*.py"),
            new FileChooser.ExtensionFilter("JavaScript文件", "*.js"),
            new FileChooser.ExtensionFilter("JSON文件", "*.json"),
            new FileChooser.ExtensionFilter("XML文件", "*.xml"),
            new FileChooser.ExtensionFilter("Markdown文件", "*.md"),
            new FileChooser.ExtensionFilter("所有文件", "*.*")
        );
        
        // Get the stage from any node in the scene
        Stage stage = (Stage) chatContainer.getScene().getWindow();
        
        // Show file chooser
        File selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile == null) {
            return; // User cancelled
        }
        
        // Validate file size (max 1MB for text files)
        long fileSizeInBytes = selectedFile.length();
        long maxSizeInBytes = 1024 * 1024; // 1MB
        
        if (fileSizeInBytes > maxSizeInBytes) {
            addMessageToChat("系统", 
                "文件过大！请选择小于 1MB 的文件。当前文件大小：" + 
                String.format("%.2f", fileSizeInBytes / 1024.0 / 1024.0) + " MB", false);
            return;
        }
        
        // Show system message about file upload
        String fileName = selectedFile.getName();
        String fileSize = String.format("%.2f KB", fileSizeInBytes / 1024.0);
        addMessageToChat("系统", 
            "📎 你上传了文件：" + fileName + " (" + fileSize + ")", false);
        
        // Disable buttons while processing
        sendButton.setDisable(true);
        uploadButton.setDisable(true);
        
        // Read and analyze file (async)
        executorService.submit(() -> {
            try {
                // Read file content
                String fileContent = Files.readString(selectedFile.toPath(), StandardCharsets.UTF_8);
                
                // Analyze file with AI service
                String aiResponse = aiService.analyzeFile(fileName, fileContent);
                
                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    addMessageToChat("AI Bot", aiResponse, false);
                    sendButton.setDisable(false);
                    uploadButton.setDisable(false);
                });
            } catch (IOException e) {
                // Log the detailed error for debugging
                System.err.println("Error reading or analyzing file: " + e.getMessage());
                e.printStackTrace();
                
                Platform.runLater(() -> {
                    String errorMsg = "读取文件时出错：" + e.getMessage();
                    if (e instanceof MalformedInputException) {
                        errorMsg = "文件编码格式不支持，请确保文件是UTF-8编码的文本文件。";
                    }
                    addMessageToChat("系统", errorMsg, false);
                    sendButton.setDisable(false);
                    uploadButton.setDisable(false);
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
