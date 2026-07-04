package com.myproject.practico.application.service;

import com.myproject.practico.application.auth.AuthenticatedIdentity;
import com.myproject.practico.application.auth.AuthenticationRequest;
import com.myproject.practico.application.auth.AuthenticationResponse;
import com.myproject.practico.application.port.in.AuthenticateUserUseCase;
import com.myproject.practico.application.port.out.IdentityPersistencePort;
import com.myproject.practico.application.port.out.TokenIssuerPort;
import com.myproject.practico.application.port.out.UserPersistencePort;
import com.myproject.practico.domain.Identity;
import com.myproject.practico.domain.User;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthenticateUserService implements AuthenticateUserUseCase {

    private final AuthenticationProviderRegistry providerRegistry;
    private final IdentityPersistencePort identityPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final TokenIssuerPort tokenIssuerPort;

    public AuthenticateUserService(
            AuthenticationProviderRegistry providerRegistry,
            IdentityPersistencePort identityPersistencePort,
            UserPersistencePort userPersistencePort,
            TokenIssuerPort tokenIssuerPort
    ) {
        this.providerRegistry = providerRegistry;
        this.identityPersistencePort = identityPersistencePort;
        this.userPersistencePort = userPersistencePort;
        this.tokenIssuerPort = tokenIssuerPort;
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        if (request == null || request.provider() == null || request.providerToken() == null || request.providerToken().isBlank()) {
            throw new IllegalArgumentException("Invalid authentication request");
        }

        AuthenticatedIdentity authenticatedIdentity = providerRegistry
                .resolve(request.provider())
                .authenticate(request.providerToken());

        Instant now = Instant.now();
        Identity identity = identityPersistencePort
                .findByProviderAndProviderSubject(authenticatedIdentity.provider(), authenticatedIdentity.providerSubject())
                .orElseGet(() -> createIdentityWithUser(authenticatedIdentity, now));

        userPersistencePort.touch(identity.userId(), now);
        return tokenIssuerPort.issueTokens(identity.userId());
    }

    private Identity createIdentityWithUser(AuthenticatedIdentity authenticatedIdentity, Instant now) {
        String displayName = authenticatedIdentity.displayName() == null || authenticatedIdentity.displayName().isBlank()
                ? authenticatedIdentity.providerSubject()
                : authenticatedIdentity.displayName();
        User user = userPersistencePort.create(displayName, now);
        return identityPersistencePort.save(new Identity(
                null,
                user.id(),
                authenticatedIdentity.provider(),
                authenticatedIdentity.providerSubject(),
                authenticatedIdentity.email(),
                displayName,
                authenticatedIdentity.avatarUrl(),
                now
        ));
    }
}
