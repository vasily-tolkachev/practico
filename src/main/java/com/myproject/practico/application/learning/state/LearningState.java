package com.myproject.practico.application.learning.state;

import com.myproject.practico.application.service.LearningPhase;

import java.util.List;
import java.util.Objects;

public record LearningState(
        int schemaVersion,
        String sessionId,
        String userId,
        LearningPhase phase,
        LearningContext context,
        ProgressSnapshot progress,
        LearningActivity currentActivity,
        List<AvailableAction> availableActions
) {
    public LearningState {
        if (schemaVersion <= 0) {
            throw new IllegalArgumentException("schemaVersion must be positive");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        Objects.requireNonNull(phase, "phase must not be null");
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(progress, "progress must not be null");
        Objects.requireNonNull(currentActivity, "currentActivity must not be null");
        availableActions = availableActions == null ? List.of() : List.copyOf(availableActions);
    }
}
