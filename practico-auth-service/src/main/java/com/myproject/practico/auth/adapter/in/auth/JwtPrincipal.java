package com.myproject.practico.auth.adapter.in.auth;

import java.util.UUID;

public record JwtPrincipal(
        UUID userId,
        UUID sessionId
) {
}
