package com.pei.controller;

import com.pei.service.AiService;
import com.pei.service.MarkdownUtil;
import com.pei.service.MarkdownTemplate;
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
import javafx.scene.web.WebView;
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
        "*.pdf","*.txt", "*.java", "*.py", "*.js", "*.json", "*.xml", "*.md", "*.csv"
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

        // 为 AI 创建一个用于流式显示的消息气泡（返回内容 Label 和包含它的 VBox）
        StreamingBubble streamingBubble = addStreamingAiMessageBubble("AI Bot");
        StringBuilder buffer = new StringBuilder();

        // 使用 DeepSeek 流式接口
        executorService.submit(() -> aiService.sendMessageStream(
                userMessage,
                delta -> {
                    // 控制台实时打印增量内容，方便调试
                    System.out.print(delta);
                    // 累积文本
                    buffer.append(delta);
                    String text = buffer.toString();
                    // 回到 JavaFX 应用线程更新回答框
                    Platform.runLater(() -> streamingBubble.contentLabel.setText(text));
                },
                error -> Platform.runLater(() -> {
                    System.err.println("Error calling AI service: " + error.getMessage());
                    error.printStackTrace();
                    addMessageToChat("系统", "抱歉，处理您的消息时出现问题，请稍后再试。", false);
                    sendButton.setDisable(false);
                }),
                () -> Platform.runLater(() -> {
                    // 流式传输完成后，将纯文本转换为 Markdown HTML 并用 WebView 替换 Label
                    String finalText = buffer.toString();
                    replaceWithMarkdownView(streamingBubble.messageBubble, streamingBubble.contentLabel, finalText);
                    sendButton.setDisable(false);
                })
        ));
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
                
                // Update UI on JavaFX thread with Markdown rendering
                Platform.runLater(() -> {
                    addMarkdownMessageToChat("AI Bot", aiResponse, false);
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
     * Add a message to the chat display（一次性文本，用于用户消息、系统提示、非流式 AI 回复）
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
     * 创建一个 AI 流式回复气泡，返回其中的内容 Label 和包含它的 VBox，供流式更新使用。
     */
    private StreamingBubble addStreamingAiMessageBubble(String sender) {
        HBox messageBox = new HBox(10);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        VBox messageBubble = new VBox(5);
        messageBubble.setMaxWidth(500);
        messageBubble.setPadding(new Insets(10));

        Label senderLabel = new Label(sender);
        senderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        Label contentLabel = new Label("");
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14px;");

        messageBubble.getChildren().addAll(senderLabel, contentLabel);
        messageBubble.getStyleClass().add("ai-message");
        messageBox.setAlignment(Pos.CENTER_LEFT);

        messageBox.getChildren().add(messageBubble);
        chatContainer.getChildren().add(messageBox);

        return new StreamingBubble(messageBubble, contentLabel);
    }

    /**
     * 添加一个使用 Markdown 渲染的消息（用于非流式的 AI 回复，如文件分析）
     */
    private void addMarkdownMessageToChat(String sender, String content, boolean isUser) {
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
        
        // Convert Markdown to HTML and display in WebView
        String htmlContent = MarkdownTemplate.wrap(MarkdownUtil.toHtml(content));
        WebView contentView = new WebView();
        contentView.getEngine().loadContent(htmlContent);
        contentView.setPrefHeight(100); // Initial height, will adjust
        contentView.setMaxWidth(480);
        
        // Adjust WebView height based on content
        contentView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    // Get document height and adjust WebView
                    String heightText = contentView.getEngine().executeScript(
                        "document.body.scrollHeight"
                    ).toString();
                    double height = Double.parseDouble(heightText);
                    contentView.setPrefHeight(height + 10);
                } catch (Exception e) {
                    // Fallback to default height
                    contentView.setPrefHeight(100);
                }
            }
        });
        
        messageBubble.getChildren().addAll(senderLabel, contentView);
        
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
     * 将流式传输完成的纯文本 Label 替换为 Markdown 渲染的 WebView
     */
    private void replaceWithMarkdownView(VBox messageBubble, Label contentLabel, String markdownText) {
        // Convert Markdown to HTML
        String htmlContent = MarkdownTemplate.wrap(MarkdownUtil.toHtml(markdownText));
        
        // Create WebView
        WebView contentView = new WebView();
        contentView.getEngine().loadContent(htmlContent);
        contentView.setPrefHeight(100); // Initial height
        contentView.setMaxWidth(480);
        
        // Adjust WebView height based on content
        contentView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                try {
                    String heightText = contentView.getEngine().executeScript(
                        "document.body.scrollHeight"
                    ).toString();
                    double height = Double.parseDouble(heightText);
                    contentView.setPrefHeight(height + 10);
                } catch (Exception e) {
                    contentView.setPrefHeight(100);
                }
            }
        });
        
        // Replace Label with WebView in the messageBubble
        int labelIndex = messageBubble.getChildren().indexOf(contentLabel);
        if (labelIndex >= 0) {
            messageBubble.getChildren().remove(labelIndex);
            messageBubble.getChildren().add(labelIndex, contentView);
        }
    }

    /**
     * 用于保存流式消息气泡的 VBox 和 Label 引用的辅助类
     */
    private static class StreamingBubble {
        final VBox messageBubble;
        final Label contentLabel;
        
        StreamingBubble(VBox messageBubble, Label contentLabel) {
            this.messageBubble = messageBubble;
            this.contentLabel = contentLabel;
        }
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
