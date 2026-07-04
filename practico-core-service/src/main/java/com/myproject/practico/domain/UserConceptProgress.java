package com.myproject.practico.domain;

import java.time.Instant;
import java.util.UUID;

public record UserConceptProgress(
        Long id,
        UUID profileId,
        Long conceptId,
        ProgressStatus status,
        int correctAnswers,
        int totalAnswers,
        Instant updatedAt
) {}
