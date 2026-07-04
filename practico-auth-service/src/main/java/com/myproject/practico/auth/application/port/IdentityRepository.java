package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.domain.AuthenticationProviderType;
import com.myproject.practico.auth.domain.Identity;

import java.util.Optional;

public interface IdentityRepository {
    Optional<Identity> findByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject);
    Identity save(Identity identity);
}
