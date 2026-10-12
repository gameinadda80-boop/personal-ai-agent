package com.example;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileChatMemoryStore implements ChatMemoryStore {

    private final File storageDirectory;
    private final ObjectMapper objectMapper;

    public FileChatMemoryStore(String directoryPath) {
        this.storageDirectory = new File(directoryPath);
        this.objectMapper = new ObjectMapper();
        if (!storageDirectory.exists()) {
            storageDirectory.mkdirs();
        }
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        File memoryFile = new File(storageDirectory, memoryId + ".json");
        if (!memoryFile.exists()) {
            return new ArrayList<>();
        }
        try {
            String json = objectMapper.readValue(memoryFile, String.class);
            return ChatMessageDeserializer.messagesFromJson(json);
        } catch (IOException e) {
            System.err.println("Error reading memory file: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        File memoryFile = new File(storageDirectory, memoryId + ".json");
        try {
            String json = ChatMessageSerializer.messagesToJson(messages);
            objectMapper.writeValue(memoryFile, json);
        } catch (IOException e) {
            System.err.println("Error writing memory file: " + e.getMessage());
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        File memoryFile = new File(storageDirectory, memoryId + ".json");
        if (memoryFile.exists()) {
            memoryFile.delete();
        }
    }
}