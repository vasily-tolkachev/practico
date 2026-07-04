package com.myproject.practico.auth.adapter.out.provider.telegram;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TelegramPayloadVerifier {

    public TelegramPrincipal verify(String providerToken) {
        if (!StringUtils.hasText(providerToken)) {
            throw new IllegalArgumentException("Telegram token must not be empty");
        }

        String subject = providerToken.trim();
        return new TelegramPrincipal(subject, "Telegram User", null, null);
    }
}
