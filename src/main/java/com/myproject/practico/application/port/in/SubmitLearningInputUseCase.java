package com.myproject.practico.application.port.in;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.service.LearningInput;

public interface SubmitLearningInputUseCase {
    LearningState submit(String userId, LearningInput input);
}
