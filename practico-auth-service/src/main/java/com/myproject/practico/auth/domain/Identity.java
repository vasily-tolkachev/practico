package com.myproject.practico.auth.domain;

import java.time.Instant;
import java.util.UUID;

public record Identity(
        UUID id,
        UUID userId,
        AuthenticationProviderType provider,
        String providerSubject,
        String email,
        String displayName,
        String avatarUrl,
        Instant createdAt
) {
}
