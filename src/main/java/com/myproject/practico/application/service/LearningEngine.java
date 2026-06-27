package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.domain.ProgressStatus;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.UserConceptProgress;

import java.time.Instant;

public class LearningEngine {

    private final EvaluationService evaluationService;
    private final UserConceptProgressService userConceptProgressService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningSessionService learningSessionService;

    public LearningEngine(
            EvaluationService evaluationService,
            UserConceptProgressService userConceptProgressService,
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService
    ) {
        this.evaluationService = evaluationService;
        this.userConceptProgressService = userConceptProgressService;
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningSessionService = learningSessionService;
    }

    public LearningResult handleAnswer(
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

        var nextDifficulty = learningSessionService.nextDifficulty(session, evaluationResult.score());
        Question nextQuestion = (conceptProgress.status() == ProgressStatus.MASTERED)
                ? getQuestionUseCase
                .getNextFromNextConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null)
                : getQuestionUseCase
                .getNextInConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);

        LearningStepType nextStep = nextQuestion == null ? LearningStepType.COMPLETED : LearningStepType.QUESTION;
        return new LearningResult(evaluationResult, conceptProgress, nextStep, nextQuestion);
    }
}
