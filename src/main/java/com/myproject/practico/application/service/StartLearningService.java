package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.StartLearningUseCase;
import com.myproject.practico.domain.Question;

import java.util.Set;

public class StartLearningService implements StartLearningUseCase {

    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningSessionService learningSessionService;
    private final LearningStateAssembler learningStateAssembler;

    public StartLearningService(
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService,
            LearningStateAssembler learningStateAssembler
    ) {
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningSessionService = learningSessionService;
        this.learningStateAssembler = learningStateAssembler;
    }

    @Override
    public LearningState start(String userId) {
        Question question = getQuestionUseCase
                .getNextFromNextConcept(null, learningSessionService.firstDifficulty(), Set.of())
                .orElse(null);

        if (question == null) {
            return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(null));
        }

        Long conceptId = question.concept() == null ? null : question.concept().id();
        learningSessionService.startLearningSession(userId, conceptId, question.id());
        return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(null));
    }
}
