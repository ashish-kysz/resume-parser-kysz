package com.example.ResumePrasing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.model}")
    private String model;

    public String getApiKey() {
        return apiKey;
    }

    public String getModel() {
        return model;
    }
}
