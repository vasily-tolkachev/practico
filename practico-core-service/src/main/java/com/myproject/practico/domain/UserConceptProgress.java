package com.myproject.practico.domain;

import java.time.Instant;

public record UserConceptProgress(
        Long id,
        Long userId,
        Long conceptId,
        ProgressStatus status,
        int correctAnswers,
        int totalAnswers,
        Instant updatedAt
) {}
