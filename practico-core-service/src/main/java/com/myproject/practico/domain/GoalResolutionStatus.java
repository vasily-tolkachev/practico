package com.myproject.practico.domain;

import java.time.Instant;

public record GoalResolutionStatus(
        Long goalId,
        GoalResolutionStage stage,
        int progressPercent,
        String message,
        Instant updatedAt
) {
}
