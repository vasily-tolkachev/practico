package com.myproject.practico.application.command.impl;

import com.myproject.practico.application.command.Command;

public class HelpCommand implements Command {

    @Override
    public boolean supports(String text) {
        return "/help".equalsIgnoreCase(text);
    }

    @Override
    public String handle(String userId, String text) {
        return "Available commands: /start, /help";
    }
}
