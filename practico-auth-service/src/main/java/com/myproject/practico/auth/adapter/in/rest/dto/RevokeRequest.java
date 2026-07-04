package com.myproject.practico.auth.adapter.in.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RevokeRequest(
        @NotNull UUID sessionId
) {
}
