package com.myproject.practico.auth.adapter.in.rest.dto;

import com.myproject.practico.auth.domain.AuthenticationProviderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull AuthenticationProviderType provider,
        @NotBlank String providerToken
) {
}
