package org.example.agents.classes;

import dev.langchain4j.service.AiServices;
import org.example.agents.TesterAgent;


public class Tester extends AgentBase {

    public TesterAgent agent;

    public Tester(String taskId) {
        super("Tester", taskId);
        ADD_PROMPT_PATH = "add-prompts/ADD_TESTER.txt";
        CreateAgent();
    }


    private String architectureContract;
    private String seniorReport;
    private int iteration;

    public void setRequest(String architectureContract, String seniorReport, int iteration) {
        this.architectureContract = architectureContract;
        this.seniorReport = seniorReport;
        this.iteration = iteration;
        Request = architectureContract;
    }

    @Override
    public void CreateAgent() {
        agent = AiServices.builder(TesterAgent.class)
                .chatModel(model)
                .tools(inspectionTool, terminalTool)
                .chatMemoryProvider(memoryProvider)
                .maxSequentialToolsInvocations(ai.settings().maxSequentialToolsInvocations())
                .build();
    }

    @Override
    public void Run() {
        String prompt = loadPrompt(ADD_PROMPT_PATH).formatted(
                limitText(architectureContract),
                limitText(seniorReport),
                iteration
        );
        Response = agent.verifyProject(memoryId, prompt);
    }
}
