package org.example;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.time.Duration;
import java.util.Map;

public final class ModelFactory {
    private ModelFactory() { }

    public static ChatModel createModel() {
        return createModel(new AISettings());
    }

    public static ChatModel createModel(AISettings settings) {
        String provider = settings.provider();

        if ("openrouter".equalsIgnoreCase(provider)) {
            require(settings.openRouterKey(), "openrouter.key");
            require(settings.openRouterUrl(), "openrouter.url");
            require(settings.openRouterModel(), "openrouter.model");

            System.out.println("AI Provider: OpenRouter");
            System.out.println("AI Model: " + settings.openRouterModel());

            return OpenAiChatModel.builder()
                    .apiKey(settings.openRouterKey())
                    .baseUrl(settings.openRouterUrl())
                    .modelName(settings.openRouterModel())
                    .temperature(settings.openRouterTemperature())
                    .timeout(Duration.ofSeconds(settings.openRouterTimeoutSeconds()))
                    .customHeaders(Map.of(
                            "HTTP-Referer", "http://localhost",
                            "X-Title", "Java-AI-System"
                    ))
                    .build();
        }

        if ("deepseek".equalsIgnoreCase(provider)) {
            require(settings.deepSeekKey(), "deepseek.key");
            require(settings.deepSeekUrl(), "deepseek.url");
            require(settings.deepSeekModel(), "deepseek.model");

            System.out.println("AI Provider: DeepSeek");
            System.out.println("AI Model: " + settings.deepSeekModel());

            return OpenAiChatModel.builder()
                    .apiKey(settings.deepSeekKey())
                    .baseUrl(settings.deepSeekUrl())
                    .modelName(settings.deepSeekModel())
                    .temperature(settings.deepSeekTemperature())
                    .timeout(Duration.ofSeconds(settings.deepSeekTimeoutSeconds()))
                    .customHeaders(Map.of(
                            "HTTP-Referer", "http://localhost",
                            "X-Title", "Java-AI-System"
                    ))
                    .build();
        }

        require(settings.lmStudioUrl(), "lmstudio.url");
        require(settings.lmStudioModel(), "lmstudio.model");

        System.out.println("AI Provider: LM Studio");
        System.out.println("AI Model: " + settings.lmStudioModel());

        return OpenAiChatModel.builder()
                .apiKey("lm-studio")
                .baseUrl(settings.lmStudioUrl())
                .modelName(settings.lmStudioModel())
                .temperature(settings.lmStudioTemperature())
                .timeout(Duration.ofSeconds(settings.lmStudioTimeoutSeconds()))
                .build();
    }

    private static void require(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Не задан " + key + " в config.properties");
        }
    }
}
