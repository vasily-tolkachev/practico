package com.myproject.practico.domain;

import java.time.Instant;

public record Identity(
        Long id,
        Long userId,
        AuthenticationProviderType provider,
        String providerSubject,
        String email,
        String displayName,
        String avatarUrl,
        Instant createdAt
) {
}
