package com.myproject.practico.domain;

import java.time.Instant;

public record Goal(
        Long id,
        String title,
        String description,
        GoalStatus status,
        Instant createdAt
) {
}
