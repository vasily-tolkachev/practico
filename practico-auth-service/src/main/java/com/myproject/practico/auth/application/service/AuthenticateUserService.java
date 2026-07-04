package com.myproject.practico.auth.application.service;

import com.myproject.practico.auth.application.dto.AuthenticatedIdentity;
import com.myproject.practico.auth.application.dto.AuthenticationRequest;
import com.myproject.practico.auth.application.dto.AuthenticationResponse;
import com.myproject.practico.auth.application.port.AuthenticateUserUseCase;
import com.myproject.practico.auth.application.port.IdentityRepository;
import com.myproject.practico.auth.application.port.RefreshSessionRepository;
import com.myproject.practico.auth.application.port.TokenIssuerPort;
import com.myproject.practico.auth.application.port.UserRepository;
import com.myproject.practico.auth.contract.TokenResponse;
import com.myproject.practico.auth.domain.Identity;
import com.myproject.practico.auth.domain.RefreshSession;
import com.myproject.practico.auth.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private static final Duration REFRESH_TTL = Duration.ofDays(30);

    private final AuthenticationProviderRegistry providerRegistry;
    private final IdentityRepository identityRepository;
    private final UserRepository userRepository;
    private final RefreshSessionRepository refreshSessionRepository;
    private final TokenIssuerPort tokenIssuerPort;
    private final TokenHashingService tokenHashingService;

    public AuthenticateUserService(
            AuthenticationProviderRegistry providerRegistry,
            IdentityRepository identityRepository,
            UserRepository userRepository,
            RefreshSessionRepository refreshSessionRepository,
            TokenIssuerPort tokenIssuerPort,
            TokenHashingService tokenHashingService
    ) {
        this.providerRegistry = providerRegistry;
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
        this.refreshSessionRepository = refreshSessionRepository;
        this.tokenIssuerPort = tokenIssuerPort;
        this.tokenHashingService = tokenHashingService;
    }

    @Override
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        AuthenticatedIdentity authenticated = providerRegistry.get(request.provider()).authenticate(request.providerToken());
        Instant now = Instant.now();

        Identity identity = identityRepository
                .findByProviderAndProviderSubject(request.provider(), authenticated.subject())
                .orElseGet(() -> createIdentity(request, authenticated, now));

        User user = userRepository.findById(identity.userId())
                .orElseThrow(() -> new IllegalStateException("User not found for identity: " + identity.id()));

        UUID sessionId = UUID.randomUUID();
        TokenResponse tokenResponse = tokenIssuerPort.issue(user, sessionId);
        RefreshSession session = new RefreshSession(
                sessionId,
                user.id(),
                tokenHashingService.hash(tokenResponse.refreshToken()),
                now.plus(REFRESH_TTL),
                now,
                null
        );
        refreshSessionRepository.save(session);

        return new AuthenticationResponse(tokenResponse);
    }

    private Identity createIdentity(AuthenticationRequest request, AuthenticatedIdentity authenticated, Instant now) {
        UUID userId = UUID.randomUUID();
        User user = new User(
                userId,
                authenticated.displayName() != null ? authenticated.displayName() : "User",
                now,
                now
        );
        userRepository.save(user);

        Identity identity = new Identity(
                UUID.randomUUID(),
                userId,
                request.provider(),
                authenticated.subject(),
                authenticated.email(),
                authenticated.displayName() != null ? authenticated.displayName() : "User",
                authenticated.avatarUrl(),
                now
        );
        return identityRepository.save(identity);
    }
}
