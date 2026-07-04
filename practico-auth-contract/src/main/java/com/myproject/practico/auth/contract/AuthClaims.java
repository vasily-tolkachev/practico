package com.myproject.practico.auth.contract;

public record AuthClaims(
        UserId userId,
        String subject,
        String provider,
        String sessionId
) {
}
