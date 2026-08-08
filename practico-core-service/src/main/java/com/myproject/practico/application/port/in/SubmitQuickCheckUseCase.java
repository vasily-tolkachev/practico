package com.myproject.practico.application.port.in;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.service.PracticeAnswer;

public interface SubmitQuickCheckUseCase {
    LearningState submitQuickCheck(String userId, PracticeAnswer answer);
}
