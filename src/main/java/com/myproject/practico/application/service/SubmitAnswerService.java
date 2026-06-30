package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
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
    private final LearningStateAssembler learningStateAssembler;

    public SubmitAnswerService(
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase,
            LearningEngine learningEngine,
            UserPersistencePort userPersistencePort,
            AnswerPersistencePort answerPersistencePort,
            LearningStateAssembler learningStateAssembler
    ) {
        this.learningSessionService = learningSessionService;
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningEngine = learningEngine;
        this.userPersistencePort = userPersistencePort;
        this.answerPersistencePort = answerPersistencePort;
        this.learningStateAssembler = learningStateAssembler;
    }

    @Override
    public LearningState submit(String userId, String answer) {
        if (answer == null || answer.isBlank()) {
            return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(null));
        }

        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return learningStateAssembler.assemble(userId, null);
        }

        Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
        if (currentQuestion == null) {
            return learningStateAssembler.assemble(userId, session);
        }

        Instant now = Instant.now();
        User user = userPersistencePort.upsertByTelegramId(userId, now);

        LearningResult learningResult;
        try {
            learningResult = session.phase() == LearningPhase.RETRY
                    ? learningEngine.handleRetryAnswer(user.id(), currentQuestion, answer, session, now)
                    : learningEngine.handleQuestionAnswer(user.id(), currentQuestion, answer, session, now);
        } catch (IllegalStateException ex) {
            return learningStateAssembler.assemble(userId, session);
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
            learningSessionService.setCurrentCycle(userId, session.currentCycle());
        } else {
            learningSessionService.setCurrentCycle(userId, null);
        }
        if (learningResult.nextPhase() == LearningPhase.QUESTION && nextQuestion != null && nextQuestion.concept() != null) {
            learningSessionService.setCurrentQuestion(userId, nextQuestion.concept().id(), nextQuestion.id());
        } else if (learningResult.nextPhase() == LearningPhase.COMPLETED) {
            learningSessionService.setCurrentQuestion(userId, null, null);
        }

        return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(session));
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
