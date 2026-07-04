package com.myproject.practico.auth.adapter.out.provider.google;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class GoogleIdTokenVerifier {

    private final boolean enabled;

    public GoogleIdTokenVerifier(@Value("${auth.providers.google.enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }

    public GooglePrincipal verify(String idToken) {
        if (!enabled) {
            throw new IllegalStateException("Google authentication provider is disabled");
        }
        if (!StringUtils.hasText(idToken)) {
            throw new IllegalArgumentException("Google id token must not be empty");
        }
        String subject = idToken.trim();
        return new GooglePrincipal(subject, null, "Google User", null);
    }
}
