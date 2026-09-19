package com.example.BajriX.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Why this class exists:
 * Configures Cross-Origin Resource Sharing (CORS).
 *
 * How it works:
 * - Our React frontend runs on http://localhost:5173 (Vite default dev server).
 * - Our Spring Boot API runs on http://localhost:8080.
 * - Browsers block cross-origin requests by default for security.
 * - This configuration explicitly authorizes the frontend on port 5173 to call all /api/** endpoints
 *   with any HTTP method (GET, POST, PUT, PATCH, DELETE) and headers.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}