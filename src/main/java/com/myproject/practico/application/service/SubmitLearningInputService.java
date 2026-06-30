package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.port.in.ContinueLearningUseCase;
import com.myproject.practico.application.port.in.GetLearningStateUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.in.SubmitLearningInputUseCase;
import com.myproject.practico.application.port.in.SubmitPracticeUseCase;
import com.myproject.practico.application.port.in.SubmitQuickCheckUseCase;
import com.myproject.practico.application.port.in.SubmitRetryUseCase;

public class SubmitLearningInputService implements SubmitLearningInputUseCase {

    private final LearningSessionService learningSessionService;
    private final SubmitAnswerUseCase submitAnswerUseCase;
    private final ContinueLearningUseCase continueLearningUseCase;
    private final SubmitPracticeUseCase submitPracticeUseCase;
    private final SubmitQuickCheckUseCase submitQuickCheckUseCase;
    private final SubmitRetryUseCase submitRetryUseCase;
    private final GetLearningStateUseCase getLearningStateUseCase;

    public SubmitLearningInputService(
            LearningSessionService learningSessionService,
            SubmitAnswerUseCase submitAnswerUseCase,
            ContinueLearningUseCase continueLearningUseCase,
            SubmitPracticeUseCase submitPracticeUseCase,
            SubmitQuickCheckUseCase submitQuickCheckUseCase,
            SubmitRetryUseCase submitRetryUseCase,
            GetLearningStateUseCase getLearningStateUseCase
    ) {
        this.learningSessionService = learningSessionService;
        this.submitAnswerUseCase = submitAnswerUseCase;
        this.continueLearningUseCase = continueLearningUseCase;
        this.submitPracticeUseCase = submitPracticeUseCase;
        this.submitQuickCheckUseCase = submitQuickCheckUseCase;
        this.submitRetryUseCase = submitRetryUseCase;
        this.getLearningStateUseCase = getLearningStateUseCase;
    }

    @Override
    public LearningState submit(String userId, LearningInput input) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return getLearningStateUseCase.getState(userId);
        }

        return switch (session.phase()) {
            case LEARNING_CARD -> continueLearningUseCase.continueLearning(userId);
            case PRACTICE -> submitPracticeUseCase.submitPractice(userId, input.practiceAnswer());
            case QUICK_CHECK -> submitQuickCheckUseCase.submitQuickCheck(userId, input.rawText());
            case RETRY -> submitRetryUseCase.submitRetry(userId, input.rawText());
            case QUESTION, COMPLETED -> submitAnswerUseCase.submit(userId, input.rawText());
        };
    }
}
