package com.myproject.practico.application.program;

import com.myproject.practico.domain.LearningProgramStatus;

import java.time.Instant;

public record ProgramGenerationStatus(
        Long programId,
        LearningProgramStatus status,
        Instant updatedAt
) {
}
