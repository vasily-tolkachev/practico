package com.myproject.practico.auth.application.service;

import com.myproject.practico.auth.application.dto.AuthenticatedIdentity;
import com.myproject.practico.auth.application.dto.AuthenticationRequest;
import com.myproject.practico.auth.application.port.AuthenticationProvider;
import com.myproject.practico.auth.application.port.IdentityRepository;
import com.myproject.practico.auth.application.port.RefreshSessionRepository;
import com.myproject.practico.auth.application.port.TokenIssuerPort;
import com.myproject.practico.auth.application.port.UserRepository;
import com.myproject.practico.auth.contract.TokenResponse;
import com.myproject.practico.auth.domain.AuthenticationProviderType;
import com.myproject.practico.auth.domain.Identity;
import com.myproject.practico.auth.domain.RefreshSession;
import com.myproject.practico.auth.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthenticateUserServiceTest {

    @Test
    void shouldProvisionUserOnFirstLogin() {
        AtomicReference<User> savedUser = new AtomicReference<>();
        AtomicReference<Identity> savedIdentity = new AtomicReference<>();
        AtomicReference<RefreshSession> savedSession = new AtomicReference<>();
        UserRepository userRepository = new UserRepository() {
            @Override public Optional<User> findById(UUID id) { return Optional.ofNullable(savedUser.get()); }
            @Override public User save(User user) { savedUser.set(user); return user; }
        };
        IdentityRepository identityRepository = new IdentityRepository() {
            @Override public Optional<Identity> findByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject) { return Optional.empty(); }
            @Override public Identity save(Identity identity) { savedIdentity.set(identity); return identity; }
        };
        RefreshSessionRepository refreshSessionRepository = new RefreshSessionRepository() {
            @Override public Optional<RefreshSession> findByTokenHash(String tokenHash) { return Optional.empty(); }
            @Override public Optional<RefreshSession> findById(UUID id) { return Optional.empty(); }
            @Override public RefreshSession save(RefreshSession session) { savedSession.set(session); return session; }
        };
        TokenIssuerPort tokenIssuerPort = (user, sessionId) -> new TokenResponse("access", "refresh", "Bearer", 3600);
        AuthenticationProvider provider = new AuthenticationProvider() {
            @Override public AuthenticationProviderType providerType() { return AuthenticationProviderType.TELEGRAM; }
            @Override public AuthenticatedIdentity authenticate(String providerToken) {
                return new AuthenticatedIdentity("subject-1", null, "Demo", null);
            }
        };

        AuthenticateUserService service = new AuthenticateUserService(
                new AuthenticationProviderRegistry(List.of(provider)),
                identityRepository,
                userRepository,
                refreshSessionRepository,
                tokenIssuerPort,
                new TokenHashingService()
        );

        var response = service.authenticate(new AuthenticationRequest(AuthenticationProviderType.TELEGRAM, "token"));

        assertEquals("access", response.tokens().accessToken());
        assertNotNull(savedUser.get());
        assertNotNull(savedIdentity.get());
        assertNotNull(savedSession.get());
        assertEquals(savedUser.get().id(), savedIdentity.get().userId());
    }
}
