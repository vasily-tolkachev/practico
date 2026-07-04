package com.myproject.practico.application.auth;

import com.myproject.practico.domain.AuthenticationProviderType;

public record AuthenticationRequest(
        AuthenticationProviderType provider,
        String providerToken
) {
}
