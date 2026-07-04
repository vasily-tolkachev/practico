package com.myproject.practico.auth.application.service;

import com.myproject.practico.auth.application.dto.AuthenticationResponse;
import com.myproject.practico.auth.application.dto.RefreshTokenRequest;
import com.myproject.practico.auth.application.port.RefreshSessionRepository;
import com.myproject.practico.auth.application.port.RefreshTokenUseCase;
import com.myproject.practico.auth.application.port.TokenIssuerPort;
import com.myproject.practico.auth.application.port.UserRepository;
import com.myproject.practico.auth.contract.TokenResponse;
import com.myproject.practico.auth.domain.RefreshSession;
import com.myproject.practico.auth.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService implements RefreshTokenUseCase {

    private static final Duration REFRESH_TTL = Duration.ofDays(30);

    private final RefreshSessionRepository refreshSessionRepository;
    private final UserRepository userRepository;
    private final TokenIssuerPort tokenIssuerPort;
    private final TokenHashingService tokenHashingService;

    public RefreshTokenService(
            RefreshSessionRepository refreshSessionRepository,
            UserRepository userRepository,
            TokenIssuerPort tokenIssuerPort,
            TokenHashingService tokenHashingService
    ) {
        this.refreshSessionRepository = refreshSessionRepository;
        this.userRepository = userRepository;
        this.tokenIssuerPort = tokenIssuerPort;
        this.tokenHashingService = tokenHashingService;
    }

    @Override
    @Transactional
    public AuthenticationResponse refresh(RefreshTokenRequest request) {
        Instant now = Instant.now();
        String tokenHash = tokenHashingService.hash(request.refreshToken());
        RefreshSession current = refreshSessionRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (!current.isActiveAt(now)) {
            throw new IllegalArgumentException("Refresh session is expired or revoked");
        }

        User user = userRepository.findById(current.userId())
                .orElseThrow(() -> new IllegalStateException("User not found for session: " + current.id()));

        refreshSessionRepository.save(new RefreshSession(
                current.id(),
                current.userId(),
                current.tokenHash(),
                current.expiresAt(),
                current.createdAt(),
                now
        ));

        UUID rotatedSessionId = UUID.randomUUID();
        TokenResponse tokenResponse = tokenIssuerPort.issue(user, rotatedSessionId);
        RefreshSession rotated = new RefreshSession(
                rotatedSessionId,
                user.id(),
                tokenHashingService.hash(tokenResponse.refreshToken()),
                now.plus(REFRESH_TTL),
                now,
                null
        );
        refreshSessionRepository.save(rotated);
        return new AuthenticationResponse(tokenResponse);
    }
}
