package com.myproject.practico.application.port.in;

import com.myproject.practico.application.learning.state.LearningState;

public interface SubmitQuickCheckUseCase {
    LearningState submitQuickCheck(String userId, String answer);
}
