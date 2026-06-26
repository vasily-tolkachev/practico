package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.HandleIncomingMessageUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.out.CommandInterpreterPort;
import com.myproject.practico.application.port.out.MessengerPort;

public class HandleIncomingMessageService implements HandleIncomingMessageUseCase {

    private final CommandInterpreterPort commandInterpreterPort;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final MessengerPort messengerPort;

    public HandleIncomingMessageService(
            CommandInterpreterPort commandInterpreterPort,
            SubmitAnswerUseCase submitAnswerUseCase,
            MessengerPort messengerPort
    ) {
        this.commandInterpreterPort = commandInterpreterPort;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.messengerPort = messengerPort;
    }

    @Override
    public void handle(String userId, String text) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        String response;
        if (text != null && text.startsWith("/")) {
            response = commandInterpreterPort.interpret(userId, text);
        } else {
            response = submitAnswerUseCase.submit(userId, text);
        }

        if (response == null || response.isBlank()) {
            return;
        }

        messengerPort.sendMessage(userId, response);
    }
}
