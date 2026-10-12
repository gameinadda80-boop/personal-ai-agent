package com.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class AgentService {

    interface Assistant {
        @SystemMessage("You are a helpful personal assistant. Use tools when needed. If the user asks you to save information to a file, use the file writing tool. Format responses with Markdown.")
        String chat(String userMessage);
    }

    private final Assistant assistant;

    public AgentService() {
        String apiKey = System.getenv("GROQ_API_KEY");
        String tavilyApiKey = System.getenv("TAVILY_API_KEY");

        ChatModel model = OpenAiChatModel.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .apiKey(apiKey)
                .modelName("openai/gpt-oss-120b")
                .temperature(0.3)
                .build();

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id("web-user")
                .maxMessages(20)
                .build();

        BasicTools basicTools = new BasicTools();
        WebSearchTool webSearchTool = new WebSearchTool(tavilyApiKey);
        WeatherTool weatherTool = new WeatherTool();
        FileWriterTool fileWriterTool = new FileWriterTool();

        // 6. Setup RAG (Document Q&A) - Defensive: skip if no documents found
        ContentRetriever contentRetriever = null;
        File documentsFolder = new File("documents");
        if (documentsFolder.exists() && documentsFolder.isDirectory() && documentsFolder.list() != null && documentsFolder.list().length > 0) {
            try {
                List<Document> documents = FileSystemDocumentLoader.loadDocuments("documents");
                if (!documents.isEmpty()) {
                    EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
                    EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

                    EmbeddingStoreIngestor.builder()
                            .documentSplitter(DocumentSplitters.recursive(300, 0))
                            .embeddingModel(embeddingModel)
                            .embeddingStore(embeddingStore)
                            .build()
                            .ingest(documents);

                    contentRetriever = EmbeddingStoreContentRetriever.builder()
                            .embeddingStore(embeddingStore)
                            .embeddingModel(embeddingModel)
                            .maxResults(2)
                            .build();
                    
                    System.out.println("RAG initialized with " + documents.size() + " document(s).");
                } else {
                    System.out.println("Warning: No documents found in 'documents' folder. RAG is disabled.");
                }
            } catch (Exception e) {
                System.err.println("Warning: Failed to load documents for RAG. RAG is disabled. Reason: " + e.getMessage());
            }
        } else {
            System.out.println("Warning: 'documents' folder is empty or missing. RAG is disabled.");
        }

        // 7. Build Assistant
        var builder = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(chatMemory)
                .tools(basicTools, webSearchTool, weatherTool, fileWriterTool);

        if (contentRetriever != null) {
            builder = builder.contentRetriever(contentRetriever);
        }

        this.assistant = builder.build();
    }

    public String chat(String message) {
        return assistant.chat(message);
    }
}