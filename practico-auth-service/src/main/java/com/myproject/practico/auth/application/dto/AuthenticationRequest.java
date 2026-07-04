package com.myproject.practico.auth.application.dto;

import com.myproject.practico.auth.domain.AuthenticationProviderType;

public record AuthenticationRequest(
        AuthenticationProviderType provider,
        String providerToken
) {
}
