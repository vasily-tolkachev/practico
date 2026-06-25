package com.myproject.practico.adapter.in.web.telegram.dto;

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
