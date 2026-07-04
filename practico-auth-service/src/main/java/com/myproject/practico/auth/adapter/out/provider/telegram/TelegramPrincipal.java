package com.myproject.practico.auth.adapter.out.provider.telegram;

public record TelegramPrincipal(
        String subject,
        String displayName,
        String username,
        String avatarUrl
) {
}
