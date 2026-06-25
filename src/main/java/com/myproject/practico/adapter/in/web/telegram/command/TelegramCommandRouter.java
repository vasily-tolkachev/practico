package com.myproject.practico.adapter.in.web.telegram.command;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TelegramCommandRouter {

    private final List<TelegramCommand> commands;

    public TelegramCommandRouter(List<TelegramCommand> commands) {
        this.commands = commands;
    }

    public String route(String userId, String text) {
        if (text == null || text.isBlank()) {
            return fallback();
        }

        for (TelegramCommand command : commands) {
            if (command.supports(text)) {
                return command.handle(userId, text);
            }
        }

        return fallback();
    }

    private String fallback() {
        return "Unknown command. Type /help";
    }
}
