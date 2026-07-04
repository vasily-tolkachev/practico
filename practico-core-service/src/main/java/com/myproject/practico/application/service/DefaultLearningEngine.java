package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.UserConceptProgress;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
            UUID userId,
            Question currentQuestion,
            String answer,
            LearningSessionStore.LearningSession session,
            Instant now
    ) {
        EvaluationResult evaluationResult = evaluationService.evaluate(
                currentQuestion.text(),
                answer,
                currentQuestion.questionType()
        );

        Long conceptId = currentQuestion.concept() == null ? null : currentQuestion.concept().id();
        if (conceptId == null) {
            throw new IllegalStateException("Current question concept was not found.");
        }

        UserConceptProgress conceptProgress = userConceptProgressService.recordAttempt(
                userId,
                conceptId,
                evaluationResult.score(),
                now
        );

        if (!evaluationResult.answeredQuestion()) {
            return new LearningResult(evaluationResult, conceptProgress, LearningPhase.LEARNING_CARD, null);
        }

        var nextDifficulty = learningSessionService.nextDifficulty(session, evaluationResult.score());
        Long currentMicroConceptId = currentQuestion.microConcept() == null ? null : currentQuestion.microConcept().id();
        Question nextQuestionInSameMicroConcept = getQuestionUseCase
                .getNextInMicroConcept(conceptId, currentMicroConceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);
        if (nextQuestionInSameMicroConcept != null) {
            return new LearningResult(evaluationResult, conceptProgress, LearningPhase.QUESTION, nextQuestionInSameMicroConcept);
        }

        Question nextQuestionInConcept = getQuestionUseCase
                .getNextFromNextMicroConcept(conceptId, currentMicroConceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);
        if (nextQuestionInConcept != null) {
            return new LearningResult(evaluationResult, conceptProgress, LearningPhase.QUESTION, nextQuestionInConcept);
        }

        UserConceptProgress masteredProgress = userConceptProgressService.markMastered(userId, conceptId, now);
        Question nextQuestion = getQuestionUseCase
                .getNextFromNextConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);

        LearningPhase nextPhase = nextQuestion == null ? LearningPhase.COMPLETED : LearningPhase.QUESTION;
        return new LearningResult(evaluationResult, masteredProgress, nextPhase, nextQuestion);
    }

    @Override
    public LearningResult handleRetryAnswer(
            UUID userId,
            Question currentQuestion,
            String answer,
            LearningSessionStore.LearningSession session,
            Instant now
    ) {
        String retryQuestionText = session.currentCycle() == null ? null : session.currentCycle().retryQuestion();
        String evaluationQuestion = (retryQuestionText == null || retryQuestionText.isBlank())
                ? currentQuestion.text()
                : retryQuestionText;
        List<String> retryRubric = session.currentCycle() == null ? List.of() : session.currentCycle().retryRubric();
        EvaluationResult evaluationResult = evaluationService.evaluateRetry(
                evaluationQuestion,
                answer,
                currentQuestion.questionType(),
                retryRubric
        );

        Long conceptId = currentQuestion.concept() == null ? null : currentQuestion.concept().id();
        if (conceptId == null) {
            throw new IllegalStateException("Current question concept was not found.");
        }

        UserConceptProgress conceptProgress = userConceptProgressService.recordAttempt(
                userId,
                conceptId,
                evaluationResult.score(),
                now
        );

        boolean quickCheckPassed = session.phase() == LearningPhase.RETRY;
        if (!retryMasteryPolicy.isMastered(evaluationResult, conceptProgress, quickCheckPassed)) {
            return new LearningResult(evaluationResult, conceptProgress, LearningPhase.RETRY, null);
        }

        var nextDifficulty = learningSessionService.nextDifficulty(session, evaluationResult.score());
        Long currentMicroConceptId = currentQuestion.microConcept() == null ? null : currentQuestion.microConcept().id();
        Question nextQuestionInSameMicroConcept = getQuestionUseCase
                .getNextInMicroConcept(conceptId, currentMicroConceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);
        if (nextQuestionInSameMicroConcept != null) {
            return new LearningResult(evaluationResult, conceptProgress, LearningPhase.QUESTION, nextQuestionInSameMicroConcept);
        }

        Question nextQuestionInConcept = getQuestionUseCase
                .getNextFromNextMicroConcept(conceptId, currentMicroConceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);
        if (nextQuestionInConcept != null) {
            return new LearningResult(evaluationResult, conceptProgress, LearningPhase.QUESTION, nextQuestionInConcept);
        }

        UserConceptProgress masteredProgress = userConceptProgressService.markMastered(userId, conceptId, now);
        Question nextQuestion = getQuestionUseCase
                .getNextFromNextConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);

        LearningPhase nextPhase = nextQuestion == null ? LearningPhase.COMPLETED : LearningPhase.QUESTION;
        return new LearningResult(evaluationResult, masteredProgress, nextPhase, nextQuestion);
    }
}
