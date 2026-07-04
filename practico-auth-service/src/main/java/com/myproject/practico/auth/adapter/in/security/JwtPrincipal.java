package com.myproject.practico.auth.adapter.in.security;

import java.util.UUID;

public record JwtPrincipal(
        UUID userId,
        UUID sessionId
) {
}
