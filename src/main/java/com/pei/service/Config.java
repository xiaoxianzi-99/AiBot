package com.pei.service;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class Config {
    private static Config INSTANCE;
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    private Config(String apiUrl, String apiKey, String model) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.model = model;
    }

    public static synchronized Config get() {
        if (INSTANCE != null) return INSTANCE;
        String apiUrl = "https://api.deepseek.com/chat/completions";
        String apiKey = "";
        String model = "deepseek-chat";
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream("application.yml")) {
            if (in != null) {
                Yaml yaml = new Yaml();
                Object data = yaml.load(in);
                if (data instanceof Map) {
                    Map<?,?> root = (Map<?,?>) data;
                    Object ds = root.get("deepseek");
                    if (ds instanceof Map) {
                        Map<?,?> dsm = (Map<?,?>) ds;
                        Object u = dsm.get("apiUrl");
                        Object k = dsm.get("apiKey");
                        Object m = dsm.get("model");
                        if (u instanceof String && !((String) u).isEmpty()) apiUrl = (String) u;
                        if (k instanceof String && !((String) k).isEmpty()) apiKey = (String) k;
                        if (m instanceof String && !((String) m).isEmpty()) model = (String) m;
                    }
                }
            }
        } catch (Exception ignored) {}
        // 如果 application.yml 中没有配置 apiKey，则尝试从环境变量读取（推荐使用 DEEPSEEK_API_KEY）
        if (apiKey == null || apiKey.isEmpty()) {
            String envKey = System.getenv("DEEPSEEK_API_KEY");
            if (envKey != null && !envKey.isEmpty()) {
                apiKey = envKey;
            }
        }
        INSTANCE = new Config(apiUrl, apiKey, model);
        return INSTANCE;
    }

    public String getApiUrl() { return apiUrl; }
    public String getApiKey() { return apiKey; }
    public String getModel() { return model; }
}
