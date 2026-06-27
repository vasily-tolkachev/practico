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
            return "Please send a non-empty answer.";
        }

        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return "No active learning session. Send /start to begin.";
        }

        Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
        if (currentQuestion == null) {
            return "Current question was not found. Send /start to restart.";
        }

        Instant now = Instant.now();
        User user = userPersistencePort.upsertByTelegramId(userId, now);

        LearningResult learningResult;
        try {
            learningResult = learningEngine.handleAnswer(user.id(), currentQuestion, answer, session, now);
        } catch (IllegalStateException ex) {
            return "Current question concept was not found. Send /start to restart.";
        }

        EvaluationResult evaluation = learningResult.evaluation();
        Question nextQuestion = learningResult.nextQuestion();
        persistAnswer(user, currentQuestion, answer, evaluation, now);

        Long nextQuestionId = switch (learningResult.nextPhase()) {
            case LEARNING_CARD -> currentQuestion.id();
            case QUESTION -> nextQuestion == null ? null : nextQuestion.id();
            case COMPLETED -> null;
            case QUICK_CHECK, RETRY -> currentQuestion.id();
        };

        learningSessionService.recordAnswerAndSetNextQuestion(userId, evaluation.score(), nextQuestionId);
        learningSessionService.setPhase(userId, learningResult.nextPhase());
        if (learningResult.nextPhase() == LearningPhase.LEARNING_CARD) {
            learningSessionService.setPendingMaterials(userId, evaluation.learningCard(), evaluation.quickCheck());
        } else {
            learningSessionService.setPendingMaterials(userId, null, null);
        }
        if (learningResult.nextPhase() == LearningPhase.QUESTION && nextQuestion != null && nextQuestion.concept() != null) {
            learningSessionService.setCurrentQuestion(userId, nextQuestion.concept().id(), nextQuestion.id());
        } else if (learningResult.nextPhase() == LearningPhase.COMPLETED) {
            learningSessionService.setCurrentQuestion(userId, null, null);
        }

        LearningSessionStore.LearningSession updatedSession = learningSessionService.getSession(userId).orElse(session);
        return buildFinalResponse(learningResult, updatedSession);
    }

    private String buildFinalResponse(
            LearningResult learningResult,
            LearningSessionStore.LearningSession session
    ) {
        EvaluationResult evaluation = learningResult.evaluation();
        StringBuilder response = new StringBuilder();
        response.append("Score: ").append(evaluation.score()).append("/10\n\n");
        response.append("Learning progress: answered ").append(session.answeredCount()).append(" questions");
        response.append(", average ").append(String.format("%.1f", learningSessionService.averageLastScores(session))).append("/10\n\n");
        response.append("Evaluation:\n").append(evaluation.evaluation());

        if (learningResult.nextPhase() == LearningPhase.LEARNING_CARD) {
            if (evaluation.learningCard() != null) {
                response.append("\n\nLearning card: ").append(evaluation.learningCard().title());
                response.append("\n").append(evaluation.learningCard().explanation());
            } else {
                response.append("\n\nLearning card:\nReview the concept and try again.");
            }
            response.append("\n\nWhen you are ready, send any message (for example, \"ready\") to move to a quick check.");
            return response.toString();
        }

        Question nextQuestion = learningResult.nextQuestion();
        if (nextQuestion != null && learningResult.nextPhase() == LearningPhase.QUESTION) {
            if (nextQuestion.concept() != null && nextQuestion.concept().topic() != null) {
                response.append("\n\nTopic: ").append(nextQuestion.concept().topic().name());
                response.append("\nConcept: ").append(nextQuestion.concept().name());
            }
            response.append("\n\nNext question:\n").append(nextQuestion.text());
        } else {
            response.append("\n\nLearning step: COMPLETED. Send /start to restart.");
        }

        return response.toString();
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
