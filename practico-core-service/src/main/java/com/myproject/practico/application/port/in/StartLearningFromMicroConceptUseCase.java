package com.myproject.practico.application.port.in;

import com.myproject.practico.application.learning.state.LearningState;

import java.util.Optional;

public interface StartLearningFromMicroConceptUseCase {

    Optional<LearningState> start(Long programId, Long microConceptId, String userId);
}
