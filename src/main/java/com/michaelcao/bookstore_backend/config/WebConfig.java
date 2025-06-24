package com.michaelcao.bookstore_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Áp dụng CORS cho tất cả các endpoint bắt đầu bằng /api/
                        .allowedOrigins("http://localhost:3000") //Cho phép request từ localhost:3000
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") //  HTTP methods được phép
                        .allowedHeaders("*") // Cho phép tất cả các header
                        .allowCredentials(true) // Cho phép cookies (cần cho HttpOnly refresh token)
                        .maxAge(3600); // Thời gian cache preflight request (1 giờ)
            }
        };
    }
}