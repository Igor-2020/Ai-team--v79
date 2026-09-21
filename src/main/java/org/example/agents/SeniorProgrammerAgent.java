package org.example.agents;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface SeniorProgrammerAgent {
    @SystemMessage(fromResource = "main-prompts/MAIN_SENIOR_DEV.txt")
    String implementFeature(@MemoryId String memoryId, @UserMessage String instructions);
}
