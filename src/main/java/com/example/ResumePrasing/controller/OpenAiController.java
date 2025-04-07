package com.example.ResumePrasing.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/openai")
public class OpenAiController {

    private final ChatModel chatClient;

    public OpenAiController( ChatModel chatClient) {
        this.chatClient = chatClient;
    }

    @GetMapping("/ask")
    public String askQuestion(@RequestParam String question) {
        return chatClient.call(question);
    }
}
