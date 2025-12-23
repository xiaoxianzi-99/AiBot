package com.pei.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * AI Service for handling AI API calls
 * This is a mock implementation that simulates AI responses
 * To use real AI APIs (like OpenAI, Claude, etc.), update the API_URL and configure API_KEY from environment
 * 
 * @author 帕斯卡的芦苇
 * @date 2025/12/23
 */
public class AiService {
    
    // Configuration - Use environment variables for production
    private static final String API_URL = System.getenv("AI_API_URL") != null 
            ? System.getenv("AI_API_URL") 
            : "https://api.example.com/chat";
    private static final String API_KEY = System.getenv("AI_API_KEY") != null 
            ? System.getenv("AI_API_KEY") 
            : "";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final Gson gson;
    private final List<JsonObject> conversationHistory;
    
    public AiService() {
        // Configure OkHttpClient with proper timeouts
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                .build();
        this.gson = new Gson();
        this.conversationHistory = new ArrayList<>();
    }
    
    /**
     * Send a message to the AI service and get a response
     * @param message User message
     * @return AI response
     * @throws IOException if network error occurs
     */
    public String sendMessage(String message) throws IOException {
        // For demo purposes, use a mock response
        // In production, replace this with actual API call
        return getMockResponse(message);
        
        // Uncomment below for real API integration:
        // return sendToRealApi(message);
    }
    
    /**
     * Mock AI response for demonstration
     * Note: The delay here simulates network latency for demonstration purposes
     * In production with real API, this delay would naturally occur during HTTP request
     * @param message User message
     * @return Mock AI response
     */
    private String getMockResponse(String message) {
        // Simulate network latency for demonstration purposes
        // In production, this would be replaced with actual async API call which has natural latency
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "抱歉，处理被中断了。";
        }
        
        // Generate contextual responses
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("你好") || lowerMessage.contains("hello") || lowerMessage.contains("hi")) {
            return "你好！很高兴见到你。我是一个AI助手，可以和你聊天、回答问题。有什么我可以帮助你的吗？";
        } else if (lowerMessage.contains("名字") || lowerMessage.contains("name")) {
            return "我是AI Bot，一个智能聊天助手。我使用JavaFX构建，可以帮助你解答问题和进行对话。";
        } else if (lowerMessage.contains("天气") || lowerMessage.contains("weather")) {
            return "抱歉，我目前还不能查询实时天气信息。这个功能可以通过集成天气API来实现。";
        } else if (lowerMessage.contains("功能") || lowerMessage.contains("能做什么") || lowerMessage.contains("what can you do")) {
            return "我可以：\n1. 和你进行自然对话\n2. 回答各种问题\n3. 提供信息和建议\n4. 学习和理解上下文\n\n注意：当前版本使用模拟响应，可以通过配置真实的AI API来获得更强大的功能。";
        } else if (lowerMessage.contains("谢谢") || lowerMessage.contains("thank")) {
            return "不客气！很高兴能帮到你。如果还有其他问题，随时问我！";
        } else if (lowerMessage.contains("再见") || lowerMessage.contains("bye")) {
            return "再见！祝你有美好的一天！";
        } else {
            return "我理解你说的是：「" + message + "」\n\n这是一个演示版本的AI助手。我已经收到你的消息，可以根据需要进行回复。如果需要接入真实的AI API（如OpenAI、Claude等），请在AiService.java中配置相应的API端点和密钥。";
        }
    }
    
    /**
     * Send message to real AI API (template for OpenAI-compatible APIs)
     * @param message User message
     * @return AI response
     * @throws IOException if network error occurs
     */
    @SuppressWarnings("unused")
    private String sendToRealApi(String message) throws IOException {
        // Add user message to history
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        conversationHistory.add(userMessage);
        
        // Build request payload
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-3.5-turbo"); // or your model
        
        JsonArray messages = new JsonArray();
        for (JsonObject msg : conversationHistory) {
            messages.add(msg);
        }
        payload.add("messages", messages);
        
        // Create request
        RequestBody body = RequestBody.create(gson.toJson(payload), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .post(body)
                .build();
        
        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("API request failed with status " + response.code() + ": " + errorBody);
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            // Extract AI response
            String aiResponse = jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
            
            // Add AI response to history
            JsonObject assistantMessage = new JsonObject();
            assistantMessage.addProperty("role", "assistant");
            assistantMessage.addProperty("content", aiResponse);
            conversationHistory.add(assistantMessage);
            
            return aiResponse;
        }
    }
}
