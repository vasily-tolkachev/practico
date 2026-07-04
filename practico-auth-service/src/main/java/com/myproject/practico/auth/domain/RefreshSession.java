package com.myproject.practico.auth.domain;

import java.time.Instant;
import java.util.UUID;

public record RefreshSession(
        UUID id,
        UUID userId,
        String tokenHash,
        Instant expiresAt,
        Instant createdAt,
        Instant revokedAt
) {
    public boolean isActiveAt(Instant now) {
        return revokedAt == null && expiresAt.isAfter(now);
    }
}
