package com.myproject.practico.auth.application.service;

import com.myproject.practico.auth.application.dto.RevokeSessionRequest;
import com.myproject.practico.auth.application.port.RefreshSessionRepository;
import com.myproject.practico.auth.application.port.RevokeSessionUseCase;
import com.myproject.practico.auth.domain.RefreshSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RevokeSessionService implements RevokeSessionUseCase {

    private final RefreshSessionRepository refreshSessionRepository;

    public RevokeSessionService(RefreshSessionRepository refreshSessionRepository) {
        this.refreshSessionRepository = refreshSessionRepository;
    }

    @Override
    @Transactional
    public void revoke(RevokeSessionRequest request) {
        RefreshSession session = refreshSessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + request.sessionId()));

        if (session.revokedAt() != null) {
            return;
        }

        refreshSessionRepository.save(new RefreshSession(
                session.id(),
                session.userId(),
                session.tokenHash(),
                session.expiresAt(),
                session.createdAt(),
                Instant.now()
        ));
    }
}
