package com.myproject.practico.config;

import com.myproject.practico.api.telegram.TelegramConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TelegramConfig.class)
public class ConfigSetup {
}