package com.example.findex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
//                .baseUrl("https://apis.data.go.kr")
                .build();
    }
}
