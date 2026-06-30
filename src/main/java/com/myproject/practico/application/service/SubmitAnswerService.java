package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.out.AnswerPersistencePort;
import com.myproject.practico.application.port.out.UserPersistencePort;
import com.myproject.practico.domain.Answer;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.User;

import java.time.Instant;

public class SubmitAnswerService implements SubmitAnswerUseCase {

    private final LearningSessionService learningSessionService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningEngine learningEngine;
    private final UserPersistencePort userPersistencePort;
    private final AnswerPersistencePort answerPersistencePort;

    public SubmitAnswerService(
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase,
            LearningEngine learningEngine,
            UserPersistencePort userPersistencePort,
            AnswerPersistencePort answerPersistencePort
    ) {
        this.learningSessionService = learningSessionService;
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningEngine = learningEngine;
        this.userPersistencePort = userPersistencePort;
        this.answerPersistencePort = answerPersistencePort;
    }

    @Override
    public String submit(String userId, String answer) {
        if (answer == null || answer.isBlank()) {
            return "Пожалуйста, отправьте непустой ответ.";
        }

        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return "Нет активной учебной сессии. Отправьте /start, чтобы начать.";
        }

        Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
        if (currentQuestion == null) {
            return "Текущий вопрос не найден. Отправьте /start, чтобы начать заново.";
        }

        Instant now = Instant.now();
        User user = userPersistencePort.upsertByTelegramId(userId, now);

        LearningResult learningResult;
        try {
            learningResult = session.phase() == LearningPhase.RETRY
                    ? learningEngine.handleRetryAnswer(user.id(), currentQuestion, answer, session, now)
                    : learningEngine.handleQuestionAnswer(user.id(), currentQuestion, answer, session, now);
        } catch (IllegalStateException ex) {
            return "Концепт текущего вопроса не найден. Отправьте /start, чтобы начать заново.";
        }

        EvaluationResult evaluation = learningResult.evaluation();
        Question nextQuestion = learningResult.nextQuestion();
        persistAnswer(user, currentQuestion, answer, evaluation, now);

        Long nextQuestionId = switch (learningResult.nextPhase()) {
            case LEARNING_CARD -> currentQuestion.id();
            case QUESTION -> nextQuestion == null ? null : nextQuestion.id();
            case COMPLETED -> null;
            case PRACTICE, QUICK_CHECK, RETRY -> currentQuestion.id();
        };

        learningSessionService.recordAnswerAndSetNextQuestion(userId, evaluation.score(), nextQuestionId);
        learningSessionService.setPhase(userId, learningResult.nextPhase());
        markCurrentMicroConceptIfCompleted(userId, currentQuestion, learningResult.nextQuestion(), learningResult.nextPhase());
        if (learningResult.nextPhase() == LearningPhase.LEARNING_CARD) {
            learningSessionService.setCurrentCycle(userId, new LearningCycle(
                    evaluation.learningCard(),
                    evaluation.quickCheck(),
                    evaluation.practiceItems(),
                    evaluation.retryRubric(),
                    evaluation.retryQuestion()
            ));
        } else if (learningResult.nextPhase() == LearningPhase.RETRY) {
            // Keep existing cycle context (practice/retryQuestion/rubric) for the next retry attempt.
            learningSessionService.setCurrentCycle(userId, session.currentCycle());
        } else {
            learningSessionService.setCurrentCycle(userId, null);
        }
        if (learningResult.nextPhase() == LearningPhase.QUESTION && nextQuestion != null && nextQuestion.concept() != null) {
            learningSessionService.setCurrentQuestion(userId, nextQuestion.concept().id(), nextQuestion.id());
        } else if (learningResult.nextPhase() == LearningPhase.COMPLETED) {
            learningSessionService.setCurrentQuestion(userId, null, null);
        }

        LearningSessionStore.LearningSession updatedSession = learningSessionService.getSession(userId).orElse(session);
        return buildFinalResponse(learningResult, currentQuestion, updatedSession);
    }

    private String buildFinalResponse(
            LearningResult learningResult,
            Question currentQuestion,
            LearningSessionStore.LearningSession session
    ) {
        EvaluationResult evaluation = learningResult.evaluation();
        StringBuilder response = new StringBuilder();
        if (learningResult.nextPhase() == LearningPhase.LEARNING_CARD) {
            response.append("Хорошее начало.");
        } else if (learningResult.nextPhase() == LearningPhase.RETRY) {
            response.append("Продолжаем.");
        } else {
            response.append("Отличное улучшение.");
        }
        response.append("\n\nОбратная связь:\n").append(conciseFeedback(evaluation.evaluation()));

        if (learningResult.nextPhase() == LearningPhase.LEARNING_CARD) {
            if (evaluation.learningCard() != null) {
                response.append("\n\nКарточка обучения: ").append(evaluation.learningCard().title());
                response.append("\n").append(evaluation.learningCard().explanation());
            } else {
                response.append("\n\nКарточка обучения:\nПросмотрите идею и попробуйте снова.");
            }
            response.append("\n\nКогда будете готовы, отправьте любое сообщение (например, \"готово\"), чтобы начать практику.");
            return response.toString();
        }

        if (learningResult.nextPhase() == LearningPhase.RETRY) {
            String retryQuestion = session == null || session.currentCycle() == null
                    ? null
                    : session.currentCycle().retryQuestion();
            if (retryQuestion == null || retryQuestion.isBlank()) {
                retryQuestion = currentQuestion == null ? null : currentQuestion.text();
            }
            if (retryQuestion != null && !retryQuestion.isBlank()) {
                response.append("\n\nПовторный вопрос:\n").append(retryQuestion);
            }
            return response.toString();
        }

        Question nextQuestion = learningResult.nextQuestion();
        if (nextQuestion != null && learningResult.nextPhase() == LearningPhase.QUESTION) {
            int order = nextQuestion.concept() == null ? 0 : getQuestionUseCase.conceptOrder(nextQuestion.concept().id());
            int total = getQuestionUseCase.totalConcepts();
            if (order > 0 && total > 0) {
                response.append("\n\nПрогресс: ").append(order).append(" / ").append(total);
                if (nextQuestion.microConcept() != null && nextQuestion.microConcept().id() != null && nextQuestion.concept() != null) {
                    int microOrder = getQuestionUseCase.microConceptOrder(nextQuestion.concept().id(), nextQuestion.microConcept().id());
                    int microTotal = getQuestionUseCase.totalMicroConcepts(nextQuestion.concept().id());
                    if (microOrder > 0 && microTotal > 0) {
                        response.append(" | Микро ").append(microOrder).append(" / ").append(microTotal);
                    }
                }
            }
            if (nextQuestion.concept() != null) {
                Long currentConceptId = currentQuestion == null || currentQuestion.concept() == null ? null : currentQuestion.concept().id();
                Long nextConceptId = nextQuestion.concept().id();
                if (currentConceptId != null && !currentConceptId.equals(nextConceptId)) {
                    response.append("\n\nСледующий концепт\n").append(nextQuestion.concept().name());
                } else {
                    response.append("\n\nКонцепт\n").append(nextQuestion.concept().name());
                }
            }
            response.append("\n\nВопрос\n").append(nextQuestion.text());
        } else {
            response.append("\n\nКурс завершён.");
            int total = getQuestionUseCase.totalConcepts();
            if (total > 0) {
                response.append("\nВы прошли ").append(total).append(" концептов.");
            }
            response.append("\nОтправьте /start, чтобы начать новый проход.");
        }

        return response.toString();
    }

    private String conciseFeedback(String feedback) {
        if (feedback == null || feedback.isBlank()) {
            return "Хорошая работа.";
        }
        String trimmed = feedback.trim();
        String[] sentences = trimmed.split("(?<=[.!?])\\s+");
        if (sentences.length <= 2) {
            return trimmed;
        }
        return (sentences[0] + " " + sentences[1]).trim();
    }

    private void markCurrentMicroConceptIfCompleted(
            String userId,
            Question currentQuestion,
            Question nextQuestion,
            LearningPhase nextPhase
    ) {
        if (currentQuestion == null || currentQuestion.microConcept() == null || currentQuestion.microConcept().id() == null) {
            return;
        }

        Long currentMicroConceptId = currentQuestion.microConcept().id();
        if (nextPhase == LearningPhase.COMPLETED) {
            learningSessionService.markMicroConceptMastered(userId, currentMicroConceptId);
            return;
        }

        if (nextPhase != LearningPhase.QUESTION || nextQuestion == null || nextQuestion.microConcept() == null || nextQuestion.microConcept().id() == null) {
            return;
        }

        Long nextMicroConceptId = nextQuestion.microConcept().id();
        if (!currentMicroConceptId.equals(nextMicroConceptId)) {
            learningSessionService.markMicroConceptMastered(userId, currentMicroConceptId);
        }
    }

    private void persistAnswer(User user, Question currentQuestion, String answerText, EvaluationResult evaluation, Instant now) {
        answerPersistencePort.save(new Answer(
                null,
                user.id(),
                currentQuestion.id(),
                answerText,
                evaluation.score(),
                evaluation.evaluation(),
                now
        ));
    }

}
