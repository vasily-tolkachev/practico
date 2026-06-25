package com.myproject.practico.api.telegram;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram")
public record TelegramConfig(
        String botToken
) {}