package com.ktb_tf_team_10_be.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // 모든 origin 허용
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:5174");
        config.addAllowedOrigin("http://3.38.107.141:8080");
        config.addAllowedOrigin("https://d21m8tnz76fb5w.cloudfront.net");



        // 모든 HTTP 메서드 허용
        config.addAllowedMethod("*");

        // 모든 헤더 허용
        config.addAllowedHeader("*");

        // 쿠키 전송 허용
        config.setAllowCredentials(true);

        // CORS 설정을 모든 경로에 적용
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
