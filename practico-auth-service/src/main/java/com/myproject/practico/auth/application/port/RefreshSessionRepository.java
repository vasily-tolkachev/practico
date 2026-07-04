package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.domain.RefreshSession;

import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionRepository {
    Optional<RefreshSession> findByTokenHash(String tokenHash);
    Optional<RefreshSession> findById(UUID id);
    RefreshSession save(RefreshSession session);
}
