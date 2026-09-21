package org.example;

import dev.langchain4j.model.chat.ChatModel;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Единый AI-контекст приложения. Модель, настройки, память и инструменты
 * создаются один раз за время работы AI Team.
 */
public final class AIEngine {
    public static final Path WORKSPACE_ROOT = Paths.get(
            System.getProperty("user.home"), "Desktop", "IITeam")
            .toAbsolutePath().normalize();

    private static AIEngine instance;

    private final AISettings settings;
    private final ChatModel model;
    private final AIMemoryManager memoryManager;
    private final AIToolRegistry tools;

    private AIEngine() {
        Path aiTeamRoot = Paths.get(System.getProperty("user.dir"))
                .toAbsolutePath().normalize();

        settings = new AISettings();
        model = ModelFactory.createModel(settings);
        memoryManager = new AIMemoryManager(settings.memoryMaxMessages());
        tools = new AIToolRegistry(WORKSPACE_ROOT, aiTeamRoot);

        System.out.println("AIEngine: общий AI-контекст создан один раз.");
        System.out.println("AIEngine: workspace = " + WORKSPACE_ROOT);
        System.out.println("AIEngine: protected root = " + aiTeamRoot);
    }

    public static synchronized AIEngine getInstance() {
        if (instance == null) instance = new AIEngine();
        return instance;
    }

    public static synchronized void initialize() {
        getInstance();
    }

    public AISettings settings() { return settings; }
    public ChatModel model() { return model; }
    public AIMemoryManager memory() { return memoryManager; }
    public AIToolRegistry tools() { return tools; }

    public String memoryId(String taskId, String agentName) {
        return taskId + ":" + agentName;
    }
}
