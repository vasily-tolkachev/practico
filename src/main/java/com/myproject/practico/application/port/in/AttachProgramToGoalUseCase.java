package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.GoalProgramLink;
import com.myproject.practico.domain.GoalProgramSourceType;

public interface AttachProgramToGoalUseCase {
    GoalProgramLink attach(Long goalId, Long programId, GoalProgramSourceType sourceType);
}
