package com.myproject.practico.adapter.in.telegram;

public interface TelegramCommand {

    boolean supports(String text);

    String handle(String userId, String text);
}
