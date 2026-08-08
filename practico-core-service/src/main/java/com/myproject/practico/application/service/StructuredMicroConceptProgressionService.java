package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.domain.Question;

public class StructuredMicroConceptProgressionService {

    private final LearningSessionService learningSessionService;
    private final GetQuestionUseCase getQuestionUseCase;

    public StructuredMicroConceptProgressionService(
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase
    ) {
        this.learningSessionService = learningSessionService;
        this.getQuestionUseCase = getQuestionUseCase;
    }

    public void advanceAfterMastery(String userId, LearningSessionStore.LearningSession session, Question currentQuestion) {
        if (currentQuestion == null || currentQuestion.concept() == null || currentQuestion.concept().id() == null) {
            return;
        }
        Long conceptId = currentQuestion.concept().id();
        Long microConceptId = currentQuestion.microConcept() == null ? null : currentQuestion.microConcept().id();
        if (microConceptId != null) {
            learningSessionService.markMicroConceptMastered(userId, microConceptId);
        }

        Question nextInConcept = getQuestionUseCase.getNextFromNextMicroConcept(
                conceptId,
                microConceptId,
                learningSessionService.firstDifficulty(),
                learningSessionService.excludedQuestionIds(session)
        ).orElse(null);
        if (nextInConcept != null && nextInConcept.concept() != null && nextInConcept.id() != null) {
            learningSessionService.setCurrentQuestion(userId, nextInConcept.concept().id(), nextInConcept.id());
            learningSessionService.setCurrentCycle(userId, null);
            learningSessionService.setPhase(userId, LearningPhase.QUESTION);
            return;
        }

        Question nextInProgram = getQuestionUseCase.getNextFromNextConcept(
                conceptId,
                learningSessionService.firstDifficulty(),
                learningSessionService.excludedQuestionIds(session)
        ).orElse(null);
        if (nextInProgram != null && nextInProgram.concept() != null && nextInProgram.id() != null) {
            learningSessionService.setCurrentQuestion(userId, nextInProgram.concept().id(), nextInProgram.id());
            learningSessionService.setCurrentCycle(userId, null);
            learningSessionService.setPhase(userId, LearningPhase.QUESTION);
            return;
        }

        learningSessionService.setCurrentQuestion(userId, null, null);
        learningSessionService.setCurrentCycle(userId, null);
        learningSessionService.setPhase(userId, LearningPhase.COMPLETED);
    }
}
