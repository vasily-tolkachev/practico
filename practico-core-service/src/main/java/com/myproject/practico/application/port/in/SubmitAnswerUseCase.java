package com.myproject.practico.application.port.in;

import com.myproject.practico.application.learning.state.LearningState;

public interface SubmitAnswerUseCase {
    LearningState submit(String userId, String answer);
}
