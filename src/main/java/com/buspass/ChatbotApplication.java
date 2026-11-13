package com.buspass;

import com.buspass.core.ChatEngine;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class ChatbotApplication {
    public static void main(String[] args) { SpringApplication.run(ChatbotApplication.class, args); }

    @Bean
    ChatEngine chatEngine() throws IOException {
        try (var in = new ClassPathResource("chatbot-kb.txt").getInputStream()) {
            return ChatEngine.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
    }

    /** Lets the Bus Pass website (another origin) call this chatbot API. Restrict origins in production. */
    @Bean
    WebMvcConfigurer cors() {
        return new WebMvcConfigurer() {
            @Override public void addCorsMappings(CorsRegistry r) { r.addMapping("/api/chat").allowedOrigins("*").allowedMethods("POST"); }
        };
    }
}
