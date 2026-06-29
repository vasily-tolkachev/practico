package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.HandleIncomingMessageUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.out.CommandInterpreterPort;
import com.myproject.practico.application.port.out.MessengerPort;

public class HandleIncomingMessageService implements HandleIncomingMessageUseCase {

    private final CommandInterpreterPort commandInterpreterPort;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final LearningSessionService learningSessionService;
    private final MessengerPort messengerPort;

    public HandleIncomingMessageService(
            CommandInterpreterPort commandInterpreterPort,
            SubmitAnswerUseCase submitAnswerUseCase,
            LearningSessionService learningSessionService,
            MessengerPort messengerPort
    ) {
        this.commandInterpreterPort = commandInterpreterPort;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.learningSessionService = learningSessionService;
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
            response = handleByPhase(userId, text);
        }

        if (response == null || response.isBlank()) {
            return;
        }

        messengerPort.sendMessage(userId, response);
    }

    private String handleByPhase(String userId, String text) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return submitAnswerUseCase.submit(userId, text);
        }

        if (session.phase() == LearningPhase.LEARNING_CARD) {
            learningSessionService.setPhase(userId, LearningPhase.QUICK_CHECK);
            if (session.currentCycle() == null
                    || session.currentCycle().quickCheck() == null
                    || session.currentCycle().quickCheck().question() == null
                    || session.currentCycle().quickCheck().question().isBlank()) {
                return "Quick check is unavailable. Please continue with the next answer.";
            }
            return "Quick check:\n" + session.currentCycle().quickCheck().question();
        }

        return submitAnswerUseCase.submit(userId, text);
    }
}
