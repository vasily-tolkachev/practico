package com.myproject.practico.auth.support;

import com.myproject.practico.auth.CurrentUserContext;
import com.myproject.practico.auth.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
            try {
                return Optional.of(UUID.fromString(value));
            } catch (IllegalArgumentException ignored) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
