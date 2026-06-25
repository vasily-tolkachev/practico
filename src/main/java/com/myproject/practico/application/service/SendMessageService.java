package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.SendMessageUseCase;
import com.myproject.practico.application.port.out.MessengerPort;

public class SendMessageService implements SendMessageUseCase {

    private final MessengerPort messengerPort;

    public SendMessageService(MessengerPort messengerPort) {
        this.messengerPort = messengerPort;
    }

    @Override
    public void send(String userId, String text) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }

        if (text == null || text.isBlank()) {
            return;
        }

        messengerPort.sendMessage(userId, text);
    }
}
