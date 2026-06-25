package com.myproject.practico.application.port.in;

public interface HandleIncomingMessageUseCase {
    void handle(String userId, String text);
}
