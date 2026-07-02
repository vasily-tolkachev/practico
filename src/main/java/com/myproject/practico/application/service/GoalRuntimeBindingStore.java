package com.myproject.practico.application.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GoalRuntimeBindingStore {

    private final Map<String, GoalRuntimeBinding> byUserId = new ConcurrentHashMap<>();

    public void bind(String userId, Long goalId, String programId) {
        if (userId == null || userId.isBlank() || goalId == null || goalId <= 0 || programId == null || programId.isBlank()) {
            return;
        }
        byUserId.put(userId, new GoalRuntimeBinding(goalId, programId, Instant.now()));
    }

    public Optional<GoalRuntimeBinding> get(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byUserId.get(userId));
    }

    public record GoalRuntimeBinding(
            Long goalId,
            String programId,
            Instant boundAt
    ) {
    }
}
