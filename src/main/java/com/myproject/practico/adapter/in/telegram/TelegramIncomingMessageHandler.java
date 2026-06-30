package com.myproject.practico.adapter.in.telegram;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.port.in.ContinueLearningUseCase;
import com.myproject.practico.application.port.in.GetLearningStateUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.in.SubmitPracticeUseCase;
import com.myproject.practico.application.port.in.SubmitQuickCheckUseCase;
import com.myproject.practico.application.port.in.SubmitRetryUseCase;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.application.service.LearningPhase;
import com.myproject.practico.application.service.LearningSessionService;
import com.myproject.practico.application.service.LearningSessionStore;
import com.myproject.practico.application.service.PracticeAnswer;
import com.myproject.practico.application.service.PracticeType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class TelegramIncomingMessageHandler {

    private final TelegramCommandRouter commandRouter;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final ContinueLearningUseCase continueLearningUseCase;
    private final SubmitPracticeUseCase submitPracticeUseCase;
    private final SubmitQuickCheckUseCase submitQuickCheckUseCase;
    private final SubmitRetryUseCase submitRetryUseCase;
    private final GetLearningStateUseCase getLearningStateUseCase;
    private final LearningSessionService learningSessionService;
    private final TelegramLearningStateRenderer renderer;
    private final TelegramPracticeAnswerParser practiceAnswerParser;
    private final MessengerPort messengerPort;

    public TelegramIncomingMessageHandler(
            TelegramCommandRouter commandRouter,
            SubmitAnswerUseCase submitAnswerUseCase,
            ContinueLearningUseCase continueLearningUseCase,
            SubmitPracticeUseCase submitPracticeUseCase,
            SubmitQuickCheckUseCase submitQuickCheckUseCase,
            SubmitRetryUseCase submitRetryUseCase,
            GetLearningStateUseCase getLearningStateUseCase,
            LearningSessionService learningSessionService,
            TelegramLearningStateRenderer renderer,
            TelegramPracticeAnswerParser practiceAnswerParser,
            MessengerPort messengerPort
    ) {
        this.commandRouter = commandRouter;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.continueLearningUseCase = continueLearningUseCase;
        this.submitPracticeUseCase = submitPracticeUseCase;
        this.submitQuickCheckUseCase = submitQuickCheckUseCase;
        this.submitRetryUseCase = submitRetryUseCase;
        this.getLearningStateUseCase = getLearningStateUseCase;
        this.learningSessionService = learningSessionService;
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

        LearningState state = handleByPhase(userId, text);
        messengerPort.sendMessage(userId, renderer.render(state));
    }

    private LearningState handleByPhase(String userId, String text) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return getLearningStateUseCase.getState(userId);
        }

        if (session.phase() == LearningPhase.LEARNING_CARD) {
            return continueLearningUseCase.continueLearning(userId);
        }
        if (session.phase() == LearningPhase.PRACTICE) {
            PracticeAnswer practiceAnswer = parsePracticeAnswer(session, text);
            return submitPracticeUseCase.submitPractice(userId, practiceAnswer);
        }
        if (session.phase() == LearningPhase.QUICK_CHECK) {
            return submitQuickCheckUseCase.submitQuickCheck(userId, text == null ? "" : text);
        }
        if (session.phase() == LearningPhase.RETRY) {
            return submitRetryUseCase.submitRetry(userId, text == null ? "" : text);
        }
        return submitAnswerUseCase.submit(userId, text == null ? "" : text);
    }

    private PracticeAnswer parsePracticeAnswer(LearningSessionStore.LearningSession session, String text) {
        List<com.myproject.practico.application.service.PracticeItem> practiceItems = session.currentCycle() == null
                ? List.of()
                : session.currentCycle().practiceItems();
        if (practiceItems == null || practiceItems.isEmpty()) {
            return new PracticeAnswer(null, Set.of());
        }
        int index = Math.max(0, Math.min(session.currentPracticeIndex(), practiceItems.size() - 1));
        PracticeType type = practiceItems.get(index).type();
        return practiceAnswerParser.parse(text, type);
    }
}
