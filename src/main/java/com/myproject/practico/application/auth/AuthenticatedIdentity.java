package com.myproject.practico.application.auth;

import com.myproject.practico.domain.AuthenticationProviderType;

public record AuthenticatedIdentity(
        AuthenticationProviderType provider,
        String providerSubject,
        String email,
        String displayName,
        String avatarUrl
) {
}
