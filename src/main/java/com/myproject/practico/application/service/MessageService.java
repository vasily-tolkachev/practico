package com.myproject.practico.application.service;

import com.myproject.practico.application.command.CommandRouter;
import com.myproject.practico.application.port.in.ProcessIncomingMessageUseCase;
import com.myproject.practico.application.port.out.MessengerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService implements ProcessIncomingMessageUseCase {

    private final CommandRouter commandRouter;
    private final MessengerPort messengerPort;

    @Override
    public void process(String userId, String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        String response = commandRouter.route(userId, text);

        messengerPort.sendMessage(userId, response);
    }
}
