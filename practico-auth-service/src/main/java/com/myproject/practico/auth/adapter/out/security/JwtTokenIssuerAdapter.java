package com.myproject.practico.auth.adapter.out.security;

import com.myproject.practico.auth.application.port.TokenIssuerPort;
import com.myproject.practico.auth.config.JwtKeyConfig;
import com.myproject.practico.auth.contract.TokenResponse;
import com.myproject.practico.auth.domain.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenIssuerAdapter implements TokenIssuerPort {

    private final JwtKeyConfig jwtKeyConfig;
    private final JwtKeyMaterial keyMaterial;

    public JwtTokenIssuerAdapter(JwtKeyConfig jwtKeyConfig, JwtKeyMaterial keyMaterial) {
        this.jwtKeyConfig = jwtKeyConfig;
        this.keyMaterial = keyMaterial;
    }

    @Override
    public TokenResponse issue(User user, UUID sessionId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(jwtKeyConfig.accessTokenTtlSeconds());
        String accessToken = Jwts.builder()
                .header().keyId(jwtKeyConfig.keyId()).and()
                .subject(user.id().toString())
                .claim("uid", user.id().toString())
                .claim("sid", sessionId.toString())
                .claim("name", user.displayName())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(keyMaterial.privateKey(), SignatureAlgorithm.RS256)
                .compact();

        String refreshToken = UUID.randomUUID() + "." + UUID.randomUUID();
        return new TokenResponse(accessToken, refreshToken, "Bearer", jwtKeyConfig.accessTokenTtlSeconds());
    }
}
