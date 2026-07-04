package com.myproject.practico.application.port.in;

import com.myproject.practico.application.learning.state.LearningState;

public interface StartLearningUseCase {
    LearningState start(String userId);
}
