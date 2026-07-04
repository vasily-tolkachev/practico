package com.myproject.practico.adapter.in.security;

import com.myproject.practico.config.AuthProperties;
import com.myproject.practico.config.JwtValidationConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtValidationFilterTest {

    private static final String SECRET = "change-me-please-change-me-please-change-me";

    @Test
    void shouldRejectInvalidSignature() throws Exception {
        JwtValidationFilter filter = new JwtValidationFilter(
                new JwtValidationConfig(SECRET),
                new AuthProperties(SECRET, 3600, 3600)
        );
        String badToken = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor("another-secret-another-secret-another-secret".getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + badToken);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        JwtValidationFilter filter = new JwtValidationFilter(
                new JwtValidationConfig(SECRET),
                new AuthProperties(SECRET, 3600, 3600)
        );
        String expired = Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + expired);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void shouldAcceptValidToken() throws Exception {
        JwtValidationFilter filter = new JwtValidationFilter(
                new JwtValidationConfig(SECRET),
                new AuthProperties(SECRET, 3600, 3600)
        );
        String uid = UUID.randomUUID().toString();
        String token = Jwts.builder()
                .subject(uid)
                .claim("uid", uid)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }
