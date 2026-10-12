package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebSearchTool {

    private static final String TAVILY_API_URL = "https://api.tavily.com/search";
    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    @Tool("Searches the web for current information. Use this when you need up-to-date facts.")
    public String searchWeb(String query) {
        System.out.println("[Tool] Searching the web for: " + query);
        
        try {
            // Create JSON request body
            String requestBody = String.format("""
                {
                    "api_key": "%s",
                    "query": "%s",
                    "search_depth": "basic",
                    "include_answer": true,
                    "max_results": 3
                }
                """, apiKey, query);

            // Send POST request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TAVILY_API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());

            // Extract the AI-generated answer from Tavily
            if (root.has("answer") && !root.get("answer").isNull()) {
                return root.get("answer").asText();
            } else {
                return "No direct answer found. Search results: " + root.get("results").toString();
            }

        } catch (Exception e) {
            System.err.println("Search failed: " + e.getMessage());
            return "Error: Could not perform web search.";
        }
    }
}