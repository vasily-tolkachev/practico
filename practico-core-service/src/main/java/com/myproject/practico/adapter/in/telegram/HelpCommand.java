package com.myproject.practico.adapter.in.telegram;

import org.springframework.stereotype.Component;

@Component
public class HelpCommand implements TelegramCommand {

    @Override
    public boolean supports(String text) {
        return "/help".equalsIgnoreCase(text);
    }

    @Override
    public String handle(String userId, String text) {
        return "Доступные команды: /start, /help\nПосле /start отправляйте ответ обычным сообщением.";
    }
}
