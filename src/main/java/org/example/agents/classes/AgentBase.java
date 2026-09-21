package org.example.agents.classes;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import org.example.AIEngine;
import org.example.tools.ProjectInspectionTool;
import org.example.tools.ProjectModificationTool;
import org.example.tools.TerminalExecutionTool;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public abstract class AgentBase {

    public static final Path WORKSPACE_ROOT = AIEngine.WORKSPACE_ROOT;
    private static final int MAX_REPORT_LENGTH = 3000;

    protected final AIEngine ai;
    protected final String taskId;
    protected final String memoryId;

    public String ADD_PROMPT_PATH;
    public String Name;

    protected final ProjectModificationTool modificationTool;
    protected final ProjectInspectionTool inspectionTool;
    protected final TerminalExecutionTool terminalTool;
    protected final ChatModel model;
    protected final ChatMemoryProvider memoryProvider;

    public String Request;
    public String Response;

    protected AgentBase(String name, String taskId) {
        this.Name = name;
        this.taskId = taskId;
        this.ai = AIEngine.getInstance();
        this.memoryId = ai.memoryId(taskId, name);

        this.model = ai.model();
        this.memoryProvider = ai.memory().provider();
        this.inspectionTool = ai.tools().inspection();
        this.modificationTool = ai.tools().modification();
        this.terminalTool = ai.tools().terminal();
    }

    public abstract void CreateAgent();
    public abstract void Run();

    protected String loadPrompt(String resourcePath) {
        try (var inputStream = AgentBase.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Prompt resource not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt: " + resourcePath, e);
        }
    }

    protected String limitText(String text) {
        if (text == null || text.isBlank()) return "(нет данных)";
        if (text.length() <= MAX_REPORT_LENGTH) return text;

        return text.substring(0, MAX_REPORT_LENGTH)
                + "\n[ОТЧЁТ СОКРАЩЁН MAINAPP]";
    }
}
