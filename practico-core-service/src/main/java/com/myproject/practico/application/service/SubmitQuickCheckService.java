package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitQuickCheckUseCase;
import com.myproject.practico.domain.Question;

public class SubmitQuickCheckService implements SubmitQuickCheckUseCase {

    private final LearningSessionService learningSessionService;
    private final PracticeService practiceService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final StructuredMicroConceptProgressionService progressionService;
    private final LearningStateAssembler learningStateAssembler;

    public SubmitQuickCheckService(
            LearningSessionService learningSessionService,
            PracticeService practiceService,
            GetQuestionUseCase getQuestionUseCase,
            StructuredMicroConceptProgressionService progressionService,
            LearningStateAssembler learningStateAssembler
    ) {
        this.learningSessionService = learningSessionService;
        this.practiceService = practiceService;
        this.getQuestionUseCase = getQuestionUseCase;
        this.progressionService = progressionService;
        this.learningStateAssembler = learningStateAssembler;
    }

    @Override
    public LearningState submitQuickCheck(String userId, PracticeAnswer answer) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return learningStateAssembler.assemble(userId, null);
        }
        if (session.currentCycle() == null || session.currentCycle().quickCheckItem() == null) {
            return learningStateAssembler.assemble(userId, session);
        }

        PracticeCheckResult checkResult = practiceService.check(answer, session.currentCycle().quickCheckItem());
        if (checkResult.correct()) {
            Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
            progressionService.advanceAfterMastery(userId, session, currentQuestion);
        } else {
            learningSessionService.setPhase(userId, LearningPhase.RETRY);
        }

        return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(session));
    }
}
