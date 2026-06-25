package com.myproject.practico.adapter.in.web.telegram.command;

public interface TelegramCommand {

    boolean supports(String text);

    String handle(String userId, String text);
}
