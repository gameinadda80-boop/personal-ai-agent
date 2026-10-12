package com.example;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final AgentService agentService;

    public ChatController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping("/chat/stream")
    public String streamChat(@RequestParam("message") String message) {
        return agentService.chat(message);
    }
}