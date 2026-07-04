package com.myproject.practico.auth.application.service;

import com.myproject.practico.auth.application.dto.RefreshTokenRequest;
import com.myproject.practico.auth.application.port.RefreshSessionRepository;
import com.myproject.practico.auth.application.port.TokenIssuerPort;
import com.myproject.practico.auth.application.port.UserRepository;
import com.myproject.practico.auth.contract.TokenResponse;
import com.myproject.practico.auth.domain.RefreshSession;
import com.myproject.practico.auth.domain.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenServiceTest {

    @Test
    void shouldRotateRefreshSession() {
        UUID userId = UUID.randomUUID();
        TokenHashingService hashing = new TokenHashingService();
        String oldRefresh = "old-refresh";
        RefreshSession existing = new RefreshSession(
                UUID.randomUUID(),
                userId,
                hashing.hash(oldRefresh),
                Instant.now().plusSeconds(3600),
                Instant.now(),
                null
        );
        List<RefreshSession> saved = new ArrayList<>();
        RefreshSessionRepository refreshSessionRepository = new RefreshSessionRepository() {
            @Override public Optional<RefreshSession> findByTokenHash(String tokenHash) { return Optional.of(existing); }
            @Override public Optional<RefreshSession> findById(UUID id) { return Optional.empty(); }
            @Override public RefreshSession save(RefreshSession session) { saved.add(session); return session; }
        };
        UserRepository userRepository = new UserRepository() {
            @Override public Optional<User> findById(UUID id) { return Optional.of(new User(userId, "Demo", Instant.now(), Instant.now())); }
            @Override public User save(User user) { return user; }
        };
        TokenIssuerPort tokenIssuerPort = (user, sessionId) -> new TokenResponse("access2", "refresh2", "Bearer", 3600);

        RefreshTokenService service = new RefreshTokenService(refreshSessionRepository, userRepository, tokenIssuerPort, hashing);
        var response = service.refresh(new RefreshTokenRequest(oldRefresh));

        assertEquals("refresh2", response.tokens().refreshToken());
        assertTrue(saved.size() >= 2);
        assertEquals(existing.id(), saved.get(0).id());
        assertTrue(saved.get(0).revokedAt() != null);
    }
}
