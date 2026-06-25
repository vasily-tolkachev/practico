package com.myproject.practico.application.port.out;

public interface CommandInterpreterPort {
    String interpret(String userId, String text);
}
