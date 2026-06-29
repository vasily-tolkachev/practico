package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.HandleIncomingMessageUseCase;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.out.CommandInterpreterPort;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.Question;

public class HandleIncomingMessageService implements HandleIncomingMessageUseCase {

    private final CommandInterpreterPort commandInterpreterPort;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningSessionService learningSessionService;
    private final QuickCheckService quickCheckService;
    private final MessengerPort messengerPort;

    public HandleIncomingMessageService(
            CommandInterpreterPort commandInterpreterPort,
            SubmitAnswerUseCase submitAnswerUseCase,
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService,
            QuickCheckService quickCheckService,
            MessengerPort messengerPort
    ) {
        this.commandInterpreterPort = commandInterpreterPort;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningSessionService = learningSessionService;
        this.quickCheckService = quickCheckService;
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

        if (session.phase() == LearningPhase.QUICK_CHECK) {
            if (session.currentCycle() == null || session.currentCycle().quickCheck() == null) {
                return "Quick check is unavailable. Please continue with the next answer.";
            }

            QuickCheckResult quickCheckResult = quickCheckService.check(text, session.currentCycle().quickCheck());
            if (!quickCheckResult.correct()) {
                String feedback = quickCheckResult.feedback() == null || quickCheckResult.feedback().isBlank()
                        ? "Not quite yet."
                        : quickCheckResult.feedback();
                return feedback + "\n\nTry again:\n" + session.currentCycle().quickCheck().question();
            }

            learningSessionService.setPhase(userId, LearningPhase.RETRY);
            Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
            if (currentQuestion == null || currentQuestion.text() == null || currentQuestion.text().isBlank()) {
                return "Correct quick check. Now retry the previous question.";
            }

            String retryQuestionText = session.currentCycle() == null ? null : session.currentCycle().retryQuestion();
            if (retryQuestionText == null || retryQuestionText.isBlank()) {
                Question retryQuestion = currentQuestion;
                if (currentQuestion.concept() != null
                        && currentQuestion.concept().id() != null
                        && currentQuestion.microConcept() != null
                        && currentQuestion.microConcept().id() != null) {
                    Question candidate = getQuestionUseCase
                            .getNextInMicroConcept(
                                    currentQuestion.concept().id(),
                                    currentQuestion.microConcept().id(),
                                    Difficulty.MEDIUM,
                                    learningSessionService.excludedQuestionIds(session)
                            )
                            .orElse(null);
                    if (candidate != null && candidate.id() != null) {
                        learningSessionService.setCurrentQuestion(userId, currentQuestion.concept().id(), candidate.id());
                        retryQuestion = candidate;
                    }
                }
                retryQuestionText = retryQuestion.text();
            }

            String feedback = quickCheckResult.feedback() == null || quickCheckResult.feedback().isBlank()
                    ? "Correct quick check."
                    : quickCheckResult.feedback();
            return feedback + "\n\nRetry question:\n" + retryQuestionText;
        }

        return submitAnswerUseCase.submit(userId, text);
    }
}
