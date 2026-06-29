package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.HandleIncomingMessageUseCase;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.out.CommandInterpreterPort;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.Question;

import java.util.List;

public class HandleIncomingMessageService implements HandleIncomingMessageUseCase {

    private final CommandInterpreterPort commandInterpreterPort;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningSessionService learningSessionService;
    private final QuickCheckService quickCheckService;
    private final PracticeService practiceService;
    private final MessengerPort messengerPort;

    public HandleIncomingMessageService(
            CommandInterpreterPort commandInterpreterPort,
            SubmitAnswerUseCase submitAnswerUseCase,
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService,
            QuickCheckService quickCheckService,
            PracticeService practiceService,
            MessengerPort messengerPort
    ) {
        this.commandInterpreterPort = commandInterpreterPort;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningSessionService = learningSessionService;
        this.quickCheckService = quickCheckService;
        this.practiceService = practiceService;
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
            List<PracticeItem> practiceItems = session.currentCycle() == null ? null : session.currentCycle().practiceItems();
            if (practiceItems == null || practiceItems.isEmpty()) {
                learningSessionService.setPhase(userId, LearningPhase.RETRY);
                String retryQuestionText = session.currentCycle() == null ? null : session.currentCycle().retryQuestion();
                if (retryQuestionText == null || retryQuestionText.isBlank()) {
                    return "Practice is unavailable. Please continue with the next answer.";
                }
                return "Retry question:\n" + retryQuestionText;
            }
            learningSessionService.setPracticeIndex(userId, 0);
            learningSessionService.setPhase(userId, LearningPhase.PRACTICE);
            return formatPracticeQuestion(practiceItems.get(0), 1, practiceItems.size());
        }

        if (session.phase() == LearningPhase.PRACTICE) {
            List<PracticeItem> practiceItems = session.currentCycle() == null ? null : session.currentCycle().practiceItems();
            if (practiceItems == null || practiceItems.isEmpty()) {
                return "Practice is unavailable. Please continue with the next answer.";
            }

            int index = Math.max(0, session.currentPracticeIndex());
            if (index >= practiceItems.size()) {
                index = practiceItems.size() - 1;
            }
            PracticeItem currentItem = practiceItems.get(index);
            PracticeCheckResult checkResult = practiceService.check(text, currentItem);
            if (!checkResult.correct()) {
                String feedback = checkResult.feedback() == null || checkResult.feedback().isBlank()
                        ? "Not quite yet."
                        : checkResult.feedback();
                return feedback + "\n\n" + formatPracticeQuestion(currentItem, index + 1, practiceItems.size());
            }

            int nextIndex = index + 1;
            if (nextIndex < practiceItems.size()) {
                learningSessionService.setPracticeIndex(userId, nextIndex);
                String feedback = checkResult.feedback() == null || checkResult.feedback().isBlank()
                        ? "Correct."
                        : checkResult.feedback();
                return feedback + "\n\n" + formatPracticeQuestion(practiceItems.get(nextIndex), nextIndex + 1, practiceItems.size());
            }

            learningSessionService.setPhase(userId, LearningPhase.RETRY);

            Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
            if (currentQuestion == null || currentQuestion.text() == null || currentQuestion.text().isBlank()) {
                return "Practice complete. Now retry the previous question.";
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

            String feedback = checkResult.feedback() == null || checkResult.feedback().isBlank()
                    ? "Practice complete."
                    : checkResult.feedback();
            return feedback + "\n\nRetry question:\n" + retryQuestionText;
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
            return "Correct. Retry question:\n" + (session.currentCycle().retryQuestion() == null ? "" : session.currentCycle().retryQuestion());
        }

        return submitAnswerUseCase.submit(userId, text);
    }

    private String formatPracticeQuestion(PracticeItem item, int index, int total) {
        StringBuilder response = new StringBuilder();
        response.append("Practice ").append(index).append("/").append(total).append(":\n");
        response.append(item.question());
        if (item.type() == PracticeType.TRUE_FALSE) {
            response.append("\n(True/False)");
        } else if (item.type() == PracticeType.MULTIPLE_CHOICE && item.options() != null && !item.options().isEmpty()) {
            for (int i = 0; i < item.options().size(); i++) {
                char label = (char) ('A' + i);
                response.append("\n").append(label).append(") ").append(item.options().get(i));
            }
            response.append("\n(Reply with letter(s), e.g. A or A,C)");
        }
        return response.toString();
    }
}
