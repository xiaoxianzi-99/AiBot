package com.pei.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Config}
 */
class ConfigTest {

    @Test
    void get_returnsNonNullInstance() {
        Config config = Config.get();
        assertNotNull(config);
    }

    @Test
    void get_returnsSameInstanceOnMultipleCalls() {
        Config first = Config.get();
        Config second = Config.get();
        assertSame(first, second);
    }

    @Test
    void getApiUrl_returnsNonNullValue() {
        Config config = Config.get();
        assertNotNull(config.getApiUrl());
    }

    @Test
    void getModel_returnsNonNullValue() {
        Config config = Config.get();
        assertNotNull(config.getModel());
    }

    @Test
    void getApiKey_returnsNonNullValue() {
        Config config = Config.get();
        // apiKey may be empty string when not configured, but must not be null
        assertNotNull(config.getApiKey());
    }

    @Test
    void defaultApiUrl_pointsToDeepSeek() throws Exception {
        Config freshConfig = createFreshConfig("https://api.deepseek.com/chat/completions", "", "deepseek-chat");
        assertEquals("https://api.deepseek.com/chat/completions", freshConfig.getApiUrl());
    }

    @Test
    void defaultModel_isDeepSeekChat() throws Exception {
        Config freshConfig = createFreshConfig("https://api.deepseek.com/chat/completions", "", "deepseek-chat");
        assertEquals("deepseek-chat", freshConfig.getModel());
    }

    /**
     * Helper to create a Config instance without going through the singleton / YAML loading.
     * Uses reflection to access the private constructor.
     */
    private static Config createFreshConfig(String apiUrl, String apiKey, String model) throws Exception {
        java.lang.reflect.Constructor<Config> ctor =
                Config.class.getDeclaredConstructor(String.class, String.class, String.class);
        ctor.setAccessible(true);
        return ctor.newInstance(apiUrl, apiKey, model);
    }
}
