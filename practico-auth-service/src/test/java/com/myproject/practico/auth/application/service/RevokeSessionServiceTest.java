package com.myproject.practico.auth.application.service;

import com.myproject.practico.auth.application.dto.RevokeSessionRequest;
import com.myproject.practico.auth.application.port.RefreshSessionRepository;
import com.myproject.practico.auth.domain.RefreshSession;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RevokeSessionServiceTest {

    @Test
    void shouldRevokeActiveSession() {
        UUID sessionId = UUID.randomUUID();
        RefreshSession active = new RefreshSession(
                sessionId,
                UUID.randomUUID(),
                "hash",
                Instant.now().plusSeconds(3600),
                Instant.now(),
                null
        );
        AtomicReference<RefreshSession> saved = new AtomicReference<>();
        RefreshSessionRepository repo = new RefreshSessionRepository() {
            @Override public Optional<RefreshSession> findByTokenHash(String tokenHash) { return Optional.empty(); }
            @Override public Optional<RefreshSession> findById(UUID id) { return Optional.of(active); }
            @Override public RefreshSession save(RefreshSession session) { saved.set(session); return session; }
        };

        RevokeSessionService service = new RevokeSessionService(repo);
        service.revoke(new RevokeSessionRequest(sessionId));

        assertNotNull(saved.get());
        assertNotNull(saved.get().revokedAt());
    }

    @Test
    void shouldFailWhenSessionMissing() {
        RefreshSessionRepository repo = new RefreshSessionRepository() {
            @Override public Optional<RefreshSession> findByTokenHash(String tokenHash) { return Optional.empty(); }
            @Override public Optional<RefreshSession> findById(UUID id) { return Optional.empty(); }
            @Override public RefreshSession save(RefreshSession session) { return session; }
        };

        RevokeSessionService service = new RevokeSessionService(repo);
        assertThrows(IllegalArgumentException.class, () -> service.revoke(new RevokeSessionRequest(UUID.randomUUID())));
    }
}
