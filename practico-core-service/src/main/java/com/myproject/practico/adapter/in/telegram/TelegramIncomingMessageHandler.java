package com.myproject.practico.adapter.in.telegram;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningActivity;
import com.myproject.practico.application.port.in.ContinueLearningUseCase;
import com.myproject.practico.application.port.in.GetLearningStateUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.in.SubmitPracticeUseCase;
import com.myproject.practico.application.port.in.SubmitQuickCheckUseCase;
import com.myproject.practico.application.port.in.SubmitRetryUseCase;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.application.service.PracticeAnswer;
import org.springframework.stereotype.Component;

@Component
public class TelegramIncomingMessageHandler {

    private final TelegramCommandRouter commandRouter;
    private final GetLearningStateUseCase getLearningStateUseCase;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final ContinueLearningUseCase continueLearningUseCase;
    private final SubmitPracticeUseCase submitPracticeUseCase;
    private final SubmitQuickCheckUseCase submitQuickCheckUseCase;
    private final SubmitRetryUseCase submitRetryUseCase;
    private final TelegramLearningStateRenderer renderer;
    private final TelegramPracticeAnswerParser practiceAnswerParser;
    private final MessengerPort messengerPort;

    public TelegramIncomingMessageHandler(
            TelegramCommandRouter commandRouter,
            GetLearningStateUseCase getLearningStateUseCase,
            SubmitAnswerUseCase submitAnswerUseCase,
            ContinueLearningUseCase continueLearningUseCase,
            SubmitPracticeUseCase submitPracticeUseCase,
            SubmitQuickCheckUseCase submitQuickCheckUseCase,
            SubmitRetryUseCase submitRetryUseCase,
            TelegramLearningStateRenderer renderer,
            TelegramPracticeAnswerParser practiceAnswerParser,
            MessengerPort messengerPort
    ) {
        this.commandRouter = commandRouter;
        this.getLearningStateUseCase = getLearningStateUseCase;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.continueLearningUseCase = continueLearningUseCase;
        this.submitPracticeUseCase = submitPracticeUseCase;
        this.submitQuickCheckUseCase = submitQuickCheckUseCase;
        this.submitRetryUseCase = submitRetryUseCase;
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

        LearningState currentState = getLearningStateUseCase.getState(userId);
        LearningState state = routeByCurrentActivity(userId, text, currentState);
        messengerPort.sendMessage(userId, renderer.render(state));
    }

    private LearningState routeByCurrentActivity(String userId, String text, LearningState currentState) {
        LearningActivity activity = currentState.currentActivity();
        String rawText = text == null ? "" : text;

        return switch (activity.type()) {
            case LEARNING_CARD -> continueLearningUseCase.continueLearning(userId);
            case PRACTICE -> {
                PracticeAnswer practiceAnswer = practiceAnswerParser.parse(rawText);
                yield submitPracticeUseCase.submitPractice(userId, practiceAnswer);
            }
            case QUICK_CHECK -> submitQuickCheckUseCase.submitQuickCheck(userId, practiceAnswerParser.parse(rawText));
            case RETRY -> submitRetryUseCase.submitRetry(userId, practiceAnswerParser.parse(rawText));
            case QUESTION -> submitAnswerUseCase.submit(userId, rawText);
            case COMPLETED -> currentState;
        };
    }
}
