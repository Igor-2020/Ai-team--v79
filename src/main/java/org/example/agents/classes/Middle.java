package org.example.agents.classes;

import dev.langchain4j.service.AiServices;
import org.example.agents.MiddleProgrammerAgent;


public class Middle extends AgentBase {

    public MiddleProgrammerAgent agent;

    public Middle(String taskId) {
        super("Middle", taskId);
        ADD_PROMPT_PATH = "add-prompts/ADD_MIDDLE_DEV.txt";
        CreateAgent();
    }


    private String architectureContract;
    private String seniorReport;

    public void setRequest(String architectureContract, String seniorReport) {
        this.architectureContract = architectureContract;
        this.seniorReport = seniorReport;
        Request = architectureContract;
    }

    @Override
    public void CreateAgent() {
        agent = AiServices.builder(MiddleProgrammerAgent.class)
                .chatModel(model)
                .tools(inspectionTool, modificationTool, terminalTool)
                .chatMemoryProvider(memoryProvider)
                .maxSequentialToolsInvocations(ai.settings().maxSequentialToolsInvocations())
                .build();
    }

    @Override
    public void Run() {
        String prompt = loadPrompt(ADD_PROMPT_PATH).formatted(
                limitText(architectureContract),
                limitText(seniorReport)
        );
        Response = agent.implementFeature(memoryId, prompt);
    }
}
