package com.ktb_tf_team_10_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private final String baseURL = "http://10.0.0.88:8000";

    @Bean
    public WebClient fastApiClient() {
        return WebClient.builder()
                .baseUrl(baseURL)
                .build();
    }
}