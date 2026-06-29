package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.domain.ProgressStatus;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.UserConceptProgress;

import java.time.Instant;

public class DefaultLearningEngine implements LearningEngine {

    private final EvaluationService evaluationService;
    private final UserConceptProgressService userConceptProgressService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningSessionService learningSessionService;
    private final RetryMasteryPolicy retryMasteryPolicy;

    public DefaultLearningEngine(
            EvaluationService evaluationService,
            UserConceptProgressService userConceptProgressService,
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService,
            RetryMasteryPolicy retryMasteryPolicy
    ) {
        this.evaluationService = evaluationService;
        this.userConceptProgressService = userConceptProgressService;
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningSessionService = learningSessionService;
        this.retryMasteryPolicy = retryMasteryPolicy;
    }

    @Override
    public LearningResult handleQuestionAnswer(
            Long userId,
            Question currentQuestion,
            String answer,
            LearningSessionStore.LearningSession session,
            Instant now
    ) {
        EvaluationResult evaluationResult = evaluationService.evaluate(currentQuestion.text(), answer);

        Long conceptId = currentQuestion.concept() == null ? null : currentQuestion.concept().id();
        if (conceptId == null) {
            throw new IllegalStateException("Current question concept was not found.");
        }

        UserConceptProgress conceptProgress = userConceptProgressService.update(
                userId,
                conceptId,
                evaluationResult.score(),
                now
        );

        if (evaluationResult.score() < 8) {
            return new LearningResult(evaluationResult, conceptProgress, LearningPhase.LEARNING_CARD, null);
        }

        var nextDifficulty = learningSessionService.nextDifficulty(session, evaluationResult.score());
        Question nextQuestion = (conceptProgress.status() == ProgressStatus.MASTERED)
                ? getQuestionUseCase
                .getNextFromNextConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null)
                : getQuestionUseCase
                .getNextInConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);

        LearningPhase nextPhase = nextQuestion == null ? LearningPhase.COMPLETED : LearningPhase.QUESTION;
        return new LearningResult(evaluationResult, conceptProgress, nextPhase, nextQuestion);
    }

    @Override
    public LearningResult handleRetryAnswer(
            Long userId,
            Question currentQuestion,
            String answer,
            LearningSessionStore.LearningSession session,
            Instant now
    ) {
        EvaluationResult evaluationResult = evaluationService.evaluate(currentQuestion.text(), answer);

        Long conceptId = currentQuestion.concept() == null ? null : currentQuestion.concept().id();
        if (conceptId == null) {
            throw new IllegalStateException("Current question concept was not found.");
        }

        UserConceptProgress conceptProgress = userConceptProgressService.update(
                userId,
                conceptId,
                evaluationResult.score(),
                now
        );

        if (!retryMasteryPolicy.isMastered(evaluationResult, conceptProgress)) {
            return new LearningResult(evaluationResult, conceptProgress, LearningPhase.LEARNING_CARD, null);
        }

        var nextDifficulty = learningSessionService.nextDifficulty(session, evaluationResult.score());
        Question nextQuestion = (conceptProgress.status() == ProgressStatus.MASTERED)
                ? getQuestionUseCase
                .getNextFromNextConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null)
                : getQuestionUseCase
                .getNextInConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);

        LearningPhase nextPhase = nextQuestion == null ? LearningPhase.COMPLETED : LearningPhase.QUESTION;
        return new LearningResult(evaluationResult, conceptProgress, nextPhase, nextQuestion);
    }
}
