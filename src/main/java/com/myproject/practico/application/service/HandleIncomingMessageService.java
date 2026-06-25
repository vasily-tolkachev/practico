package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.HandleIncomingMessageUseCase;
import com.myproject.practico.application.port.out.CommandInterpreterPort;
import com.myproject.practico.application.port.out.MessengerPort;

public class HandleIncomingMessageService implements HandleIncomingMessageUseCase {

    private final CommandInterpreterPort commandInterpreterPort;
    private final MessengerPort messengerPort;

    public HandleIncomingMessageService(
            CommandInterpreterPort commandInterpreterPort,
            MessengerPort messengerPort
    ) {
        this.commandInterpreterPort = commandInterpreterPort;
        this.messengerPort = messengerPort;
    }

    @Override
    public void handle(String userId, String text) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        String response = commandInterpreterPort.interpret(userId, text);

        if (response == null || response.isBlank()) {
            return;
        }

        messengerPort.sendMessage(userId, response);
    }
}
