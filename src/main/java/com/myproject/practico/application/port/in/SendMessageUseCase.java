package com.myproject.practico.application.port.in;

public interface SendMessageUseCase {
    void send(String userId, String text);
}
