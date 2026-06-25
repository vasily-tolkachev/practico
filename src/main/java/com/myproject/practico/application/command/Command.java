package com.myproject.practico.application.command;

public interface Command {

    boolean supports(String text);

    String handle(String userId, String text);
}