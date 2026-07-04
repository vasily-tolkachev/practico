package com.myproject.practico.auth.contract;

import java.util.UUID;

public record AuthClaims(
        UUID userId,
        String subject,
        String provider,
        String sessionId
) {
}
