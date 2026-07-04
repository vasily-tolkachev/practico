package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.SubmitQuickCheckUseCase;

public class SubmitQuickCheckService implements SubmitQuickCheckUseCase {

    private final LearningSessionService learningSessionService;
    private final QuickCheckService quickCheckService;
    private final LearningStateAssembler learningStateAssembler;

    public SubmitQuickCheckService(
            LearningSessionService learningSessionService,
            QuickCheckService quickCheckService,
            LearningStateAssembler learningStateAssembler
    ) {
        this.learningSessionService = learningSessionService;
        this.quickCheckService = quickCheckService;
        this.learningStateAssembler = learningStateAssembler;
    }

    @Override
    public LearningState submitQuickCheck(String userId, String answer) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return learningStateAssembler.assemble(userId, null);
        }
        if (session.currentCycle() == null || session.currentCycle().quickCheck() == null) {
            return learningStateAssembler.assemble(userId, session);
        }

        QuickCheckResult quickCheckResult = quickCheckService.check(answer, session.currentCycle().quickCheck());
        if (quickCheckResult.correct()) {
            learningSessionService.setPhase(userId, LearningPhase.RETRY);
        }

        return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(session));
    }
}
