package com.myproject.practico.domain;

import java.time.Instant;

public record LearningProgram(
        Long id,
        String title,
        String description,
        LearningProgramStatus status,
        LearningProgramOrigin origin,
        Instant createdAt,
        Instant updatedAt
) {
}
