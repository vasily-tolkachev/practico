package com.myproject.practico.application.command;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CommandRouter {

    private final List<Command> commands;

    public CommandRouter(List<Command> commands) {
        this.commands = commands;
    }

    public String route(String userId, String text) {

        for (Command command : commands) {
            if (command.supports(text)) {
                return command.handle(userId, text);
            }
        }

        return fallback(text);
    }

    private String fallback(String text) {
        return "Unknown command. Type /help";
    }
}