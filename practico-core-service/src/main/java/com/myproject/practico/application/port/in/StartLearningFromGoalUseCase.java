package com.myproject.practico.application.port.in;

import com.myproject.practico.application.goal.GoalLearningStartResult;

import java.util.Optional;

public interface StartLearningFromGoalUseCase {
    Optional<GoalLearningStartResult> start(Long goalId, String userId);
}
