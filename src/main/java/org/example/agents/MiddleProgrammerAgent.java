package org.example.agents;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface MiddleProgrammerAgent {
    @SystemMessage(fromResource = "main-prompts/MAIN_MIDDLE_DEV.txt")
    String implementFeature(@MemoryId String memoryId, @UserMessage String instructions);
}
