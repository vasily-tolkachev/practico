package com.myproject.practico.api.telegram.dto;

public record TelegramUpdate(
        Message message
) {
    public record Message(
            Chat chat,
            String text
    ) {}

    public record Chat(
            String id
    ) {}
}