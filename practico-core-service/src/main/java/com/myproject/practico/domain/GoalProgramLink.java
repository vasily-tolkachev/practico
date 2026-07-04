package com.myproject.practico.domain;

import java.time.Instant;

public record GoalProgramLink(
        Long id,
        Long goalId,
        Long programId,
        GoalProgramSourceType sourceType,
        Instant createdAt
) {
}
