package com.myproject.practico.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth.jwt")
public record JwtKeyConfig(
        String keyId,
        long accessTokenTtlSeconds
) {
    public JwtKeyConfig {
        if (keyId == null || keyId.isBlank()) {
            keyId = "practico-auth-k1";
        }
        if (accessTokenTtlSeconds <= 0) {
            accessTokenTtlSeconds = 3600;
        }
    }
}
