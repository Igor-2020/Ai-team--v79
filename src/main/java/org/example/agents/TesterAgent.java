package org.example.agents;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface TesterAgent {
@SystemMessage(fromResource = "main-prompts/MAIN_TESTER.txt")
    String verifyProject(@MemoryId String memoryId, @UserMessage String instruction);
}