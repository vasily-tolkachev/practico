package com.myproject.practico.auth.adapter.out.provider.telegram;

import com.myproject.practico.auth.application.dto.AuthenticatedIdentity;
import com.myproject.practico.auth.application.port.AuthenticationProvider;
import com.myproject.practico.auth.domain.AuthenticationProviderType;
import org.springframework.stereotype.Component;

@Component
public class TelegramAuthenticationProvider implements AuthenticationProvider {

    private final TelegramPayloadVerifier verifier;

    public TelegramAuthenticationProvider(TelegramPayloadVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public AuthenticationProviderType providerType() {
        return AuthenticationProviderType.TELEGRAM;
    }

    @Override
    public AuthenticatedIdentity authenticate(String providerToken) {
        TelegramPrincipal principal = verifier.verify(providerToken);
        return new AuthenticatedIdentity(
                principal.subject(),
                principal.username(),
                principal.displayName(),
                principal.avatarUrl()
        );
    }
}
