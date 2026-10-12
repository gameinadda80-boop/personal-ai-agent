package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import dev.langchain4j.agent.tool.Tool;

public class BasicTools {
    @Tool("Gets the current date and time. Use this when the user asks for the current time or date.")
    public String getCurrentDateTime() {
        System.out.println("[Tool] Getting current date and time");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");
        return now.format(formatter);
    }
    @Tool("Calculates the sum of two numbers")
    public double add(double a, double b) {
        System.out.println("[Tool] Adding " + a + " and " + b);
        return a + b;
    }
}