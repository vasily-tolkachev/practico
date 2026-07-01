package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.GoalResolutionStatus;

import java.util.Optional;

public interface GoalResolutionStatusPort {
    void save(GoalResolutionStatus status);

    Optional<GoalResolutionStatus> findByGoalId(Long goalId);
}
