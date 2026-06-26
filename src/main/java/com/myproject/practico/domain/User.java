package com.myproject.practico.domain;

import java.time.Instant;

public record User(
        Long id,
        String telegramId,
        Instant createdAt,
        Instant lastSeen
) {}
