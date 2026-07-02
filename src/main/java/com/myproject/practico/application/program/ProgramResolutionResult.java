package com.myproject.practico.application.program;

import com.myproject.practico.domain.GoalProgramSourceType;

public record ProgramResolutionResult(
        LearningProgram program,
        GoalProgramSourceType sourceType
) {
}
