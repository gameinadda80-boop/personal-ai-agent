package com.example;

import dev.langchain4j.agent.tool.Tool;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileWriterTool {

    @Tool("Writes text content to a file with the given name. Use this tool to save information, notes, or summaries to a file.")
    public String writeToFile(String fileName, String content) {
        try {
            // Ensure the filename ends with .txt if no extension is provided
            if (!fileName.contains(".")) {
                fileName += ".txt";
            }

            // Save files inside an "output" folder to keep things tidy
            File outputDir = new File("output");
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }

            File file = new File(outputDir, fileName);
            
            // Write the content to the file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(content);
            }

            System.out.println("[Tool] Successfully wrote to file: " + file.getAbsolutePath());
            return "Successfully saved content to " + fileName;

        } catch (IOException e) {
            System.err.println("[Tool] Error writing file: " + e.getMessage());
            return "Error writing to file: " + e.getMessage();
        }
    }
}