package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.Tool;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class WeatherTool {

    @Tool("Gets the current weather for a given city. Use this when the user asks about the weather.")
    public String getWeather(String city) {
        System.out.println("[Tool] Getting weather for: " + city);
        try {
            HttpClient client = HttpClient.newHttpClient();
            ObjectMapper mapper = new ObjectMapper();

            // 1. Get Latitude and Longitude for the city
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
            String geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" + encodedCity + "&count=1";
            
            HttpRequest geoRequest = HttpRequest.newBuilder().uri(URI.create(geoUrl)).GET().build();
            HttpResponse<String> geoResponse = client.send(geoRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode geoRoot = mapper.readTree(geoResponse.body());

            if (!geoRoot.has("results") || geoRoot.get("results").isEmpty()) {
                return "Could not find coordinates for city: " + city;
            }

            double lat = geoRoot.get("results").get(0).get("latitude").asDouble();
            double lon = geoRoot.get("results").get(0).get("longitude").asDouble();

            // 2. Get Weather using coordinates
            String weatherUrl = String.format("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&current=temperature_2m,wind_speed_10m", lat, lon);
            HttpRequest weatherRequest = HttpRequest.newBuilder().uri(URI.create(weatherUrl)).GET().build();
            HttpResponse<String> weatherResponse = client.send(weatherRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode weatherRoot = mapper.readTree(weatherResponse.body());

            double temp = weatherRoot.get("current").get("temperature_2m").asDouble();
            double wind = weatherRoot.get("current").get("wind_speed_10m").asDouble();

            return String.format("The current weather in %s is %s°C with a wind speed of %s km/h.", city, temp, wind);

        } catch (Exception e) {
            System.err.println("Weather tool error: " + e.getMessage());
            return "Error: Could not retrieve weather data.";
        }
    }
}