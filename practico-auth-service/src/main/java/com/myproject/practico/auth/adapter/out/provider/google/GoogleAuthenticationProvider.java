package com.myproject.practico.auth.adapter.out.provider.google;

import com.myproject.practico.auth.application.dto.AuthenticatedIdentity;
import com.myproject.practico.auth.application.port.AuthenticationProvider;
import com.myproject.practico.auth.domain.AuthenticationProviderType;
import org.springframework.stereotype.Component;

@Component
public class GoogleAuthenticationProvider implements AuthenticationProvider {

    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthenticationProvider(GoogleIdTokenVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public AuthenticationProviderType providerType() {
        return AuthenticationProviderType.GOOGLE;
    }

    @Override
    public AuthenticatedIdentity authenticate(String providerToken) {
        GooglePrincipal principal = verifier.verify(providerToken);
        return new AuthenticatedIdentity(
                principal.subject(),
                principal.email(),
                principal.displayName(),
                principal.avatarUrl()
        );
    }
}
