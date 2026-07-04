package com.myproject.practico.adapter.out.telegram;

import com.myproject.practico.application.port.out.MessengerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelegramAdapter implements MessengerPort {

    private final TelegramClient telegramClient;

    @Override
    public void sendMessage(String userId, String text) {
        telegramClient.sendMessage(userId, text);
    }
}
