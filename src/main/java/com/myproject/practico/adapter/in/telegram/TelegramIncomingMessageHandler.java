package com.myproject.practico.adapter.in.telegram;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.port.in.SubmitLearningInputUseCase;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.application.service.LearningInput;
import com.myproject.practico.application.service.PracticeAnswer;
import org.springframework.stereotype.Component;

@Component
public class TelegramIncomingMessageHandler {

    private final TelegramCommandRouter commandRouter;
    private final SubmitLearningInputUseCase submitLearningInputUseCase;
    private final TelegramLearningStateRenderer renderer;
    private final TelegramPracticeAnswerParser practiceAnswerParser;
    private final MessengerPort messengerPort;

    public TelegramIncomingMessageHandler(
            TelegramCommandRouter commandRouter,
            SubmitLearningInputUseCase submitLearningInputUseCase,
            TelegramLearningStateRenderer renderer,
            TelegramPracticeAnswerParser practiceAnswerParser,
            MessengerPort messengerPort
    ) {
        this.commandRouter = commandRouter;
        this.submitLearningInputUseCase = submitLearningInputUseCase;
        this.renderer = renderer;
        this.practiceAnswerParser = practiceAnswerParser;
        this.messengerPort = messengerPort;
    }

    public void handle(String userId, String text) {
        if (userId == null || userId.isBlank()) {
            return;
        }

        if (text != null && text.startsWith("/")) {
            String commandResponse = commandRouter.interpret(userId, text);
            if (commandResponse != null && !commandResponse.isBlank()) {
                messengerPort.sendMessage(userId, commandResponse);
            }
            return;
        }

        PracticeAnswer practiceAnswer = practiceAnswerParser.parse(text);
        LearningState state = submitLearningInputUseCase.submit(userId, new LearningInput(text, practiceAnswer));
        messengerPort.sendMessage(userId, renderer.render(state));
    }
}
