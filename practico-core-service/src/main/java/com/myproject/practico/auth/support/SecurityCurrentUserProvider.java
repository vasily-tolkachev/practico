package com.myproject.practico.auth.support;

import com.myproject.practico.auth.CurrentUserContext;
import com.myproject.practico.auth.CurrentUserProvider;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Optional<UUID> currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CurrentUserContext context) {
            return Optional.ofNullable(context.userId());
        }
        if (principal instanceof String value && !value.isBlank()) {
            return toUserUuid(value);
        }
        if (principal instanceof Jwt jwt) {
            String uid = jwt.getClaimAsString("uid");
            if (uid == null || uid.isBlank()) {
                uid = jwt.getSubject();
            }
            if (uid == null || uid.isBlank()) {
                return Optional.empty();
            }
            return toUserUuid(uid);
        }
        return Optional.empty();
    }

    private Optional<UUID> toUserUuid(String rawUserId) {
        if (rawUserId == null || rawUserId.isBlank()) {
            return Optional.empty();
        }
        String normalized = rawUserId.trim();
        try {
            return Optional.of(UUID.fromString(normalized));
        } catch (IllegalArgumentException ignored) {
            return Optional.of(UUID.nameUUIDFromBytes(normalized.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
