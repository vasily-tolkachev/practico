package com.myproject.practico.application.port.in;

public interface ProcessIncomingMessageUseCase {
    void process(String userId, String text);
}