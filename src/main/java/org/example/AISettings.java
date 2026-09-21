package org.example;

import java.io.InputStream;
import java.util.Properties;

/** Общие настройки AI Team. Загружаются один раз при создании AIEngine. */
public final class AISettings {
    private final Properties properties = new Properties();

    public AISettings() {
        try (InputStream input = AISettings.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) throw new RuntimeException("Не найден файл config.properties в resources!");
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки конфигурации AI Team", e);
        }
    }

    public String provider() { return properties.getProperty("ai.provider", "lm-studio"); }
    public String openRouterKey() { return properties.getProperty("openrouter.key"); }
    public String openRouterUrl() { return properties.getProperty("openrouter.url"); }
    public String openRouterModel() { return properties.getProperty("openrouter.model"); }
    public double openRouterTemperature() { return doubleProperty("openrouter.temperature", 0.1); }
    public long openRouterTimeoutSeconds() { return longProperty("openrouter.timeoutSeconds", 600); }
    public String deepSeekKey() { return properties.getProperty("deepseek.key"); }
    public String deepSeekUrl() { return properties.getProperty("deepseek.url"); }
    public String deepSeekModel() { return properties.getProperty("deepseek.model"); }
    public double deepSeekTemperature() { return doubleProperty("deepseek.temperature", 0.1); }
    public long deepSeekTimeoutSeconds() { return longProperty("deepseek.timeoutSeconds", 600); }
    public String lmStudioUrl() { return properties.getProperty("lmstudio.url"); }
    public String lmStudioModel() { return properties.getProperty("lmstudio.model"); }
    public double lmStudioTemperature() { return doubleProperty("lmstudio.temperature", 0.1); }
    public long lmStudioTimeoutSeconds() { return longProperty("lmstudio.timeoutSeconds", 600); }
    public int memoryMaxMessages() { return intProperty("ai.memory.maxMessages", 20); }
    public int maxSequentialToolsInvocations() { return intProperty("ai.maxSequentialToolsInvocations", 36); }

    private double doubleProperty(String key, double fallback) {
        return Double.parseDouble(properties.getProperty(key, String.valueOf(fallback)));
    }
    private long longProperty(String key, long fallback) {
        return Long.parseLong(properties.getProperty(key, String.valueOf(fallback)));
    }
    private int intProperty(String key, int fallback) {
        return Integer.parseInt(properties.getProperty(key, String.valueOf(fallback)));
    }

}
