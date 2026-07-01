package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.GoalResolutionStatus;

import java.util.Optional;

public interface GetGoalResolutionStatusUseCase {
    Optional<GoalResolutionStatus> getByGoalId(Long goalId);
}
