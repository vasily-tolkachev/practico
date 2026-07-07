package com.myproject.practico.adapter.out.telegram;

import com.myproject.practico.config.TelegramProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "telegram.enabled", havingValue = "true")
public class TelegramClient {

    private final TelegramProperties config;

    public void sendMessage(String chatId, String text) {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://api.telegram.org/bot" + config.botToken())
                .build();

        restClient.post()
                .uri("/sendMessage")
                .body(new SendMessageRequest(chatId, text))
                .retrieve()
                .toBodilessEntity();
    }

    private record SendMessageRequest(
            String chat_id,
            String text
    ) {}
}
