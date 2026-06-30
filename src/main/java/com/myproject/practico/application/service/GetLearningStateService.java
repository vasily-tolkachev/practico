package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.GetLearningStateUseCase;

public class GetLearningStateService implements GetLearningStateUseCase {

    private final LearningSessionService learningSessionService;
    private final LearningStateAssembler learningStateAssembler;

    public GetLearningStateService(
            LearningSessionService learningSessionService,
            LearningStateAssembler learningStateAssembler
    ) {
        this.learningSessionService = learningSessionService;
        this.learningStateAssembler = learningStateAssembler;
    }

    @Override
    public LearningState getState(String userId) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        return learningStateAssembler.assemble(userId, session);
    }
}
