package com.myproject.practico.auth.dto;

import com.myproject.practico.domain.AuthenticationProviderType;

public record LoginCommand(
        AuthenticationProviderType provider,
        String providerToken
) {
}
