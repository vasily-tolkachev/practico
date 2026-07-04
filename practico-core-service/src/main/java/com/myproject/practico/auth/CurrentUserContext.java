package com.myproject.practico.auth;

import java.util.UUID;

public record CurrentUserContext(
        UUID userId,
        UUID sessionId
) {
}
