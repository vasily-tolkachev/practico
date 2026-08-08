package com.myproject.practico.application.port.in;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.service.PracticeAnswer;

public interface SubmitRetryUseCase {
    LearningState submitRetry(String userId, PracticeAnswer answer);
}
