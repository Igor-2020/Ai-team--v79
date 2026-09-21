package org.example;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Единая точка управления памятью всех агентов. Память изолируется по memoryId. */
public final class AIMemoryManager {
    private final int maxMessages;
    private final Map<Object, ChatMemory> memories = new ConcurrentHashMap<>();

    public AIMemoryManager(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    public ChatMemory getMemory(Object memoryId) {
        return memories.computeIfAbsent(memoryId, id ->
                MessageWindowChatMemory.withMaxMessages(maxMessages));
    }

    public ChatMemoryProvider provider() {
        return this::getMemory;
    }

    public void clear(Object memoryId) {
        memories.remove(memoryId);
    }

    public void clearAll() {
        memories.clear();
    }

    public int size() {
        return memories.size();
    }
}
