package com.myproject.practico.application.port.in;

import com.myproject.practico.application.learning.state.LearningState;

public interface SubmitRetryUseCase {
    LearningState submitRetry(String userId, String answer);
}
