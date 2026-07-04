package com.myproject.practico.application.port.in;

import com.myproject.practico.application.program.ProgramResolutionResult;
import com.myproject.practico.domain.Goal;

public interface ProgramResolverUseCase {
    ProgramResolutionResult resolveForGoal(Goal goal, String userId);
}
