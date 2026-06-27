package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.domain.ProgressStatus;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.UserConceptProgress;

import java.time.Instant;

public class LearningEngine {

    private final AiEvaluationService aiEvaluationService;
    private final UserConceptProgressService userConceptProgressService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningSessionService learningSessionService;

    public LearningEngine(
            AiEvaluationService aiEvaluationService,
            UserConceptProgressService userConceptProgressService,
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService
    ) {
        this.aiEvaluationService = aiEvaluationService;
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
        AiResponse aiResponse = aiEvaluationService.evaluate(currentQuestion.text(), answer);

        Long conceptId = currentQuestion.concept() == null ? null : currentQuestion.concept().id();
        if (conceptId == null) {
            throw new IllegalStateException("Current question concept was not found.");
        }

        UserConceptProgress conceptProgress = userConceptProgressService.update(
                userId,
                conceptId,
                aiResponse.score(),
                now
        );

        String nextDifficulty = learningSessionService.nextDifficulty(session, aiResponse.score());
        Question nextQuestion = (conceptProgress.status() == ProgressStatus.MASTERED)
                ? getQuestionUseCase
                .getNextFromNextConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null)
                : getQuestionUseCase
                .getNextInConcept(conceptId, nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);

        return new LearningResult(aiResponse, conceptProgress, nextQuestion);
    }
}
