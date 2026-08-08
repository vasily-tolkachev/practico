package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitRetryUseCase;
import com.myproject.practico.domain.Question;

public class SubmitRetryService implements SubmitRetryUseCase {

    private final LearningSessionService learningSessionService;
    private final PracticeService practiceService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final StructuredMicroConceptProgressionService progressionService;
    private final LearningStateAssembler learningStateAssembler;

    public SubmitRetryService(
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
    public LearningState submitRetry(String userId, PracticeAnswer answer) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return learningStateAssembler.assemble(userId, null);
        }
        if (session.currentCycle() == null || session.currentCycle().retryItem() == null) {
            return learningStateAssembler.assemble(userId, session);
        }

        PracticeCheckResult checkResult = practiceService.check(answer, session.currentCycle().retryItem());
        if (!checkResult.correct()) {
            return learningStateAssembler.assemble(userId, session);
        }

        Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
        progressionService.advanceAfterMastery(userId, session, currentQuestion);
        return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(session));
    }
}
