package com.myproject.practico.auth.application.dto;

import java.util.UUID;

public record RevokeSessionRequest(
        UUID sessionId
) {
}
