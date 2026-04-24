package com.smartdesk.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.embedding.EmbeddingModel;

@Configuration
public class AIConfig {

    @Bean
    ChatClient chatClient(OllamaChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    // EmbeddingModel bean is auto-provided by Spring AI starter
}