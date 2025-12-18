package com.ktb_tf_team_10_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

//    private final String baseURL = "http://54.180.137.99:8000";
//    private final String baseURL = "http://3.34.50.107:8000";

//    @Bean
//    public WebClient fastApiClient() {
//        return WebClient.builder()
//                .baseUrl(baseURL)
//                .clientConnector(
//                        new ReactorClientHttpConnector(
//                                HttpClient.create()
//                                        .responseTimeout(Duration.ofMinutes(15))
//                        )
//                )
//                .build();
//    }

    @Bean
    public WebClient fastApi2DClient() {
        return WebClient.builder()
                .baseUrl("http://3.34.50.107:8000")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofMinutes(15))
                ))
                .build();
    }

    @Bean
    public WebClient fastApi3DClient() {
        return WebClient.builder()
                .baseUrl("http://54.180.137.99:8000")
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofMinutes(15))
                ))
                .build();
    }

}