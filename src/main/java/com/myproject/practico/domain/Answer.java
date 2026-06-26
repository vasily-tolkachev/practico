package com.myproject.practico.domain;

import java.time.Instant;

public record Answer(
        Long id,
        Long userId,
        Long questionId,
        String answer,
        int score,
        String feedback,
        Instant createdAt
) {}
