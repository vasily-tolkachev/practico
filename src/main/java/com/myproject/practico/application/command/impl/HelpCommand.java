package com.myproject.practico.application.command.impl;

import com.myproject.practico.application.command.Command;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements Command {

    @Override
    public boolean supports(String text) {
        return text.equalsIgnoreCase("/help");
    }

    @Override
    public String handle(String userId, String text) {
        return "Available commands: ping, help";
    }
}