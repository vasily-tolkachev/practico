package com.myproject.practico.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt-validation")
public record JwtValidationConfig(
        String jwksUri
) {
}
