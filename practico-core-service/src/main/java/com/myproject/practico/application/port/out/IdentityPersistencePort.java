package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.AuthenticationProviderType;
import com.myproject.practico.domain.Identity;

import java.util.List;
import java.util.Optional;

public interface IdentityPersistencePort {
    Optional<Identity> findByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject);
    Identity save(Identity identity);
    boolean existsByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject);
    List<Identity> findByUserId(Long userId);
}
