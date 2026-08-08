package com.myproject.practico.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final String LOCAL_WEB_ORIGIN = "http://localhost:5173";
    private static final String LOCAL_WEB_ORIGIN_LOOPBACK = "http://127.0.0.1:5173";
    private static final String PROD_WEB_ORIGIN = "https://mastery-project.quest";
    private static final String PROD_WEB_ORIGIN_WWW = "https://www.mastery-project.quest";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(LOCAL_WEB_ORIGIN, LOCAL_WEB_ORIGIN_LOOPBACK, PROD_WEB_ORIGIN, PROD_WEB_ORIGIN_WWW)
                .allowedMethods("GET", "POST", "PATCH", "OPTIONS")
                .allowedHeaders("*");

        registry.addMapping("/goals/**")
                .allowedOrigins(LOCAL_WEB_ORIGIN, LOCAL_WEB_ORIGIN_LOOPBACK, PROD_WEB_ORIGIN, PROD_WEB_ORIGIN_WWW)
                .allowedMethods("GET", "POST", "PATCH", "OPTIONS")
                .allowedHeaders("*");

        registry.addMapping("/programs/**")
                .allowedOrigins(LOCAL_WEB_ORIGIN, LOCAL_WEB_ORIGIN_LOOPBACK, PROD_WEB_ORIGIN, PROD_WEB_ORIGIN_WWW)
                .allowedMethods("GET", "POST", "PATCH", "OPTIONS")
                .allowedHeaders("*");
    }
}
