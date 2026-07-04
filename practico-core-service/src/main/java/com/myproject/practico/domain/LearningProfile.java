package com.myproject.practico.domain;

import java.time.Instant;
import java.util.UUID;

public record LearningProfile(
        UUID id,
        String displayName,
        Instant createdAt,
        Instant updatedAt
) {
}
