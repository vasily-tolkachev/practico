package com.myproject.practico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String LOCAL_WEB_ORIGIN = "http://localhost:5173";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(LOCAL_WEB_ORIGIN)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");

        registry.addMapping("/goals/**")
                .allowedOrigins(LOCAL_WEB_ORIGIN)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");

        registry.addMapping("/programs/**")
                .allowedOrigins(LOCAL_WEB_ORIGIN)
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }
}
