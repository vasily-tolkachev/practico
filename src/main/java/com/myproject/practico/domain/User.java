package com.myproject.practico.domain;

import java.time.Instant;

public record User(
        Long id,
        String displayName,
        Instant createdAt,
        Instant updatedAt
) {}
