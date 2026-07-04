package com.myproject.practico.adapter.out.security;

import com.myproject.practico.application.auth.AuthenticationResponse;
import com.myproject.practico.application.port.out.TokenIssuerPort;
import com.myproject.practico.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

    private final AuthProperties authProperties;
    private final SecretKey signingKey;

    public JwtTokenIssuerAdapter(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.signingKey = Keys.hmacShaKeyFor(authProperties.jwtSecret().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public AuthenticationResponse issueTokens(Long userId) {
        Instant now = Instant.now();
        String userIdValue = String.valueOf(userId);
        String accessToken = buildToken(userIdValue, now, authProperties.accessTokenTtlSeconds(), "access");
        String refreshToken = buildToken(userIdValue, now, authProperties.refreshTokenTtlSeconds(), "refresh");
        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String buildToken(String userId, Instant now, long ttlSeconds, String tokenType) {
        return Jwts.builder()
                .subject(userId)
                .claim("type", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(signingKey)
                .compact();
    }
}
