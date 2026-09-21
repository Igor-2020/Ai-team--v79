package org.example.agents.classes;

import dev.langchain4j.service.AiServices;
import org.example.agents.SeniorProgrammerAgent;


public class Senior extends AgentBase {

    public static final String IMPLEMENT_PROMPT_PATH = "add-prompts/ADD_SENIOR_DEV.txt";
    public static final String FIX_PROMPT_PATH = "add-prompts/SENIOR_FIX.txt";

    public SeniorProgrammerAgent agent;

    public Senior(String taskId) {
        super("Senior", taskId);
        CreateAgent();
    }

    private String architectureContract;
    private String previousDeveloperReport;
    private String testerReport;
    private int iteration;


    @Override
    public void CreateAgent() {
        agent = AiServices.builder(SeniorProgrammerAgent.class)
                .chatModel(model)
                .tools(inspectionTool, modificationTool, terminalTool)
                .chatMemoryProvider(memoryProvider)
                .maxSequentialToolsInvocations(ai.settings().maxSequentialToolsInvocations())
                .build();
    }

    public void setImplementationRequest(String architectureContract) {
        ADD_PROMPT_PATH = IMPLEMENT_PROMPT_PATH;
        this.architectureContract = architectureContract;
        Request = architectureContract;
    }

    public void setFixRequest(String architectureContract, String previousDeveloperReport,
                              String testerReport, int iteration) {
        ADD_PROMPT_PATH = FIX_PROMPT_PATH;
        this.architectureContract = architectureContract;
        this.previousDeveloperReport = previousDeveloperReport;
        this.testerReport = testerReport;
        this.iteration = iteration;
        Request = architectureContract;
    }

    @Override
    public void Run() {
        String prompt;

        if (FIX_PROMPT_PATH.equals(ADD_PROMPT_PATH)) {
            prompt = loadPrompt(ADD_PROMPT_PATH).formatted(
                    limitText(architectureContract),
                    limitText(previousDeveloperReport),
                    limitText(testerReport),
                    iteration
            );
        } else {
            prompt = loadPrompt(ADD_PROMPT_PATH).formatted(limitText(architectureContract));
        }

        Response = agent.implementFeature(memoryId, prompt);
    }
}
