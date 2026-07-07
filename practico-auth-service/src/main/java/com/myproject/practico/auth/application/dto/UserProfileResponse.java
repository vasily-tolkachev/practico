package com.myproject.practico.auth.application.dto;

import java.time.Instant;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String displayName,
        String email,
        Instant createdAt
) {
}
