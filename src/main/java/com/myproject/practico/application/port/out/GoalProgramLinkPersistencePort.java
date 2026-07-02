package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.GoalProgramLink;
import com.myproject.practico.domain.GoalProgramSourceType;

import java.util.Optional;

public interface GoalProgramLinkPersistencePort {
    GoalProgramLink create(Long goalId, String programId, GoalProgramSourceType sourceType);

    Optional<GoalProgramLink> findByGoalId(Long goalId);

    Optional<GoalProgramLink> findByGoalIdAndProgramId(Long goalId, String programId);
}
