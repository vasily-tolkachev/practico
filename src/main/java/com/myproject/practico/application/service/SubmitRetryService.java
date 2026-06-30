package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.in.SubmitRetryUseCase;

public class SubmitRetryService implements SubmitRetryUseCase {

    private final SubmitAnswerUseCase submitAnswerUseCase;

    public SubmitRetryService(SubmitAnswerUseCase submitAnswerUseCase) {
        this.submitAnswerUseCase = submitAnswerUseCase;
    }

    @Override
    public LearningState submitRetry(String userId, String answer) {
        return submitAnswerUseCase.submit(userId, answer);
    }
}
