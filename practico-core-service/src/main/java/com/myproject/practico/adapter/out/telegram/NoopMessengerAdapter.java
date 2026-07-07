package com.myproject.practico.adapter.out.telegram;

import com.myproject.practico.application.port.out.MessengerPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "telegram.enabled", havingValue = "false", matchIfMissing = true)
public class NoopMessengerAdapter implements MessengerPort {

    @Override
    public void sendMessage(String userId, String text) {
        // Telegram disabled in this profile.
    }
}
