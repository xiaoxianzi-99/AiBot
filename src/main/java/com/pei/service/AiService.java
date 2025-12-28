package com.pei.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import okhttp3.*;
import okio.BufferedSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * AI Service for handling AI API calls
 * 深度集成 DeepSeek Chat Completions 接口，支持普通请求和流式输出
 */
public class AiService {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Gson gson;
    private final List<JsonObject> conversationHistory;
    private final Config config;

    public AiService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS) // 流式请求不设置读超时
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                .build();
        this.gson = new Gson();
        this.conversationHistory = new ArrayList<>();
        this.config = Config.get();
    }

    /**
     * 非流式：发送消息并一次性获取完整回复
     */
    public String sendMessage(String message) throws IOException {
        JsonObject payload = buildPayload(message, false);
        RequestBody body = RequestBody.create(gson.toJson(payload), JSON);

        Request request = new Request.Builder()
                .url(config.getApiUrl())
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No error details";
                throw new IOException("DeepSeek API request failed with status " + response.code() + ": " + errorBody);
            }
            String responseBody = response.body() != null ? response.body().string() : "";
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                return "DeepSeek 没有返回任何回复。";
            }
            JsonObject choice0 = choices.get(0).getAsJsonObject();
            JsonObject msg = choice0.getAsJsonObject("message");
            if (msg == null || !msg.has("content")) {
                return "DeepSeek 返回的格式不包含 message.content。";
            }
            String content = msg.get("content").getAsString();

            // 记录到对话历史
            JsonObject assistantMessage = new JsonObject();
            assistantMessage.addProperty("role", "assistant");
            assistantMessage.addProperty("content", content);
            conversationHistory.add(assistantMessage);

            return content;
        }
    }

    /**
     * 流式：发送消息，逐步通过 onDelta 回调输出增量文本。
     */
    public void sendMessageStream(String message,
                                  Consumer<String> onDelta,
                                  Consumer<Throwable> onError,
                                  Runnable onComplete) {
        JsonObject payload = buildPayload(message, true);
        RequestBody body = RequestBody.create(gson.toJson(payload), JSON);

        Request request = new Request.Builder()
                .url(config.getApiUrl())
                .header("Authorization", "Bearer " + config.getApiKey())
                .header("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (onError != null) {
                    onError.accept(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    String errorBody;
                    try {
                        errorBody = response.body() != null ? response.body().string() : "No error details";
                    } catch (IOException e) {
                        errorBody = "No error details (" + e.getMessage() + ")";
                    }
                    if (onError != null) {
                        onError.accept(new IOException("DeepSeek API request failed with status " + response.code() + ": " + errorBody));
                    }
                    if (onComplete != null) onComplete.run();
                    return;
                }

                try (ResponseBody body = response.body()) {
                    if (body == null) {
                        if (onError != null) {
                            onError.accept(new IOException("DeepSeek 响应体为空"));
                        }
                        if (onComplete != null) onComplete.run();
                        return;
                    }
                    BufferedSource source = body.source();
                    while (!source.exhausted()) {
                        String line = source.readUtf8LineStrict();
                        if (line == null || line.isEmpty()) continue;
                        // DeepSeek 兼容 OpenAI 流式：每行以 "data: " 开头，最后一行为 "data: [DONE]"
                        if (line.startsWith("data: ")) {
                            String jsonPart = line.substring("data: ".length()).trim();
                            if ("[DONE]".equals(jsonPart)) {
                                break;
                            }
                            try {
                                JsonObject chunk = gson.fromJson(jsonPart, JsonObject.class);
                                JsonArray choices = chunk.getAsJsonArray("choices");
                                if (choices == null || choices.size() == 0) continue;
                                JsonObject deltaObj = choices.get(0).getAsJsonObject().getAsJsonObject("delta");
                                if (deltaObj == null) continue;

                                // DeepSeek 可能使用 content 或 role 等字段，这里只关注 content 的增量
                                JsonElement contentEl = deltaObj.get("content");
                                if (contentEl != null && !contentEl.isJsonNull()) {
                                    String piece = contentEl.getAsString();
                                    if (!piece.isEmpty() && onDelta != null) {
                                        onDelta.accept(piece);
                                    }
                                }
                            } catch (Exception parseEx) {
                                // 单个 chunk 解析错误不影响整体流，打印后继续
                                if (onError != null) {
                                    onError.accept(parseEx);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    if (onError != null) {
                        onError.accept(e);
                    }
                } finally {
                    if (onComplete != null) onComplete.run();
                }
            }
        });
    }

    /**
     * 构建 DeepSeek 请求 payload
     */
    private JsonObject buildPayload(String message, boolean stream) {
        // 先把用户消息加入历史
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", message);
        conversationHistory.add(userMessage);

        JsonObject payload = new JsonObject();
        payload.addProperty("model", config.getModel());

        JsonArray messages = new JsonArray();
        for (JsonObject msg : conversationHistory) {
            messages.add(msg);
        }
        payload.add("messages", messages);

        payload.addProperty("stream", stream);
        return payload;
    }

    /**
     * 旧的 mock 逻辑已不再使用，如需离线演示可单独保留一个 MockAiService。
     */

    /**
     * 分析文件：将文件名和内容包装成提示词，交给 DeepSeek 处理。
     */
    public String analyzeFile(String fileName, String fileContent) throws IOException {
        String prompt = "我上传了一个文件：" + fileName + "\n\n" +
                "文件内容如下：\n" +
                "```\n" + fileContent + "\n```\n\n" +
                "请用中文详细分析这个文件的结构、重点内容，并给出对我有帮助的解读和建议。";
        return sendMessage(prompt);
    }
}
