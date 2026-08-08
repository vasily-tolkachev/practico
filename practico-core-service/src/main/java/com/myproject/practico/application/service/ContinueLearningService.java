package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.ContinueLearningUseCase;

import java.util.List;

public class ContinueLearningService implements ContinueLearningUseCase {

    private final LearningSessionService learningSessionService;
    private final LearningStateAssembler learningStateAssembler;

    public ContinueLearningService(
            LearningSessionService learningSessionService,
            LearningStateAssembler learningStateAssembler
    ) {
        this.learningSessionService = learningSessionService;
        this.learningStateAssembler = learningStateAssembler;
    }

    @Override
    public LearningState continueLearning(String userId) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return learningStateAssembler.assemble(userId, null);
        }

        if (session.phase() == LearningPhase.LEARNING_CARD) {
            LearningCycle cycle = session.currentCycle();
            List<?> practiceItems = cycle == null || cycle.practiceItems() == null ? List.of() : cycle.practiceItems();
            if (!practiceItems.isEmpty()) {
                learningSessionService.setPracticeIndex(userId, 0);
                learningSessionService.setPhase(userId, LearningPhase.PRACTICE);
            } else if (cycle != null && cycle.quickCheckItem() != null) {
                learningSessionService.setPhase(userId, LearningPhase.QUICK_CHECK);
            } else {
                learningSessionService.setPhase(userId, LearningPhase.RETRY);
            }
        } else if (session.phase() == LearningPhase.QUESTION && session.currentCycle() != null) {
            learningSessionService.setPhase(userId, LearningPhase.LEARNING_CARD);
        }

        return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(session));
    }
}
