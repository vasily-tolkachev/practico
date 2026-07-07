package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.domain.AuthenticationProviderType;
import com.myproject.practico.auth.domain.Identity;

import java.util.Optional;
import java.util.UUID;

public interface IdentityRepository {
    Optional<Identity> findByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject);
    Optional<Identity> findFirstByUserId(UUID userId);
    Identity save(Identity identity);
}
