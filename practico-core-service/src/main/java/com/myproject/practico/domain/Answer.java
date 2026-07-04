package com.myproject.practico.domain;

import java.time.Instant;
import java.util.UUID;

public record Answer(
        Long id,
        UUID profileId,
        Long questionId,
        String answer,
        int score,
        String feedback,
        Instant createdAt
) {}
