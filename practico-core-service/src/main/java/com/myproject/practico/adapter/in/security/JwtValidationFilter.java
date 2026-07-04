package com.myproject.practico.adapter.in.security;

import com.myproject.practico.auth.CurrentUserContext;
import com.myproject.practico.config.AuthProperties;
import com.myproject.practico.config.JwtValidationConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    private final SecretKey signingKey;

    public JwtValidationFilter(JwtValidationConfig jwtValidationConfig, AuthProperties authProperties) {
        String secret = jwtValidationConfig.sharedSecret();
        if (secret == null || secret.isBlank()) {
            secret = authProperties.jwtSecret();
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
            String uid = claims.get("uid", String.class);
            String sid = claims.get("sid", String.class);
            if (uid == null || uid.isBlank()) {
                uid = claims.getSubject();
            }
            if (uid != null && !uid.isBlank() && SecurityContextHolder.getContext().getAuthentication() == null) {
                CurrentUserContext principal = new CurrentUserContext(
                        UUID.fromString(uid),
                        sid == null || sid.isBlank() ? null : UUID.fromString(sid)
                );
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
