package org.example.agents.classes;

import dev.langchain4j.service.AiServices;
import org.example.agents.ArchitectAgent;


public class Architect extends AgentBase {

    public ArchitectAgent agent;

    public Architect(String request, String taskId) {
        super("Architect", taskId);
        ADD_PROMPT_PATH = "add-prompts/ADD_ARCHITECT_DEV.txt";
        Request = request;
        CreateAgent();
    }


    @Override
    public void CreateAgent() {
        agent = AiServices.builder(ArchitectAgent.class)
                .chatModel(model)
                .tools(inspectionTool)
                .chatMemoryProvider(memoryProvider)
                .maxSequentialToolsInvocations(ai.settings().maxSequentialToolsInvocations())
                .build();
    }

    @Override
    public void Run() {
        String prompt = loadPrompt(ADD_PROMPT_PATH).formatted(Request, WORKSPACE_ROOT);
        Response = agent.implementFeature(memoryId, prompt);
    }
}
