package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.application.port.out.LearningProfilePersistencePort;
import com.myproject.practico.auth.CurrentUserProvider;
import com.myproject.practico.domain.LearningProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final CurrentUserProvider currentUserProvider;
    private final LearningProfilePersistencePort learningProfilePersistencePort;

    public ProfileController(
            CurrentUserProvider currentUserProvider,
            LearningProfilePersistencePort learningProfilePersistencePort
    ) {
        this.currentUserProvider = currentUserProvider;
        this.learningProfilePersistencePort = learningProfilePersistencePort;
    }

    @GetMapping
    public ResponseEntity<LearningProfile> getProfile() {
        UUID userId = currentUserProvider.currentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        Instant now = Instant.now();
        LearningProfile profile = learningProfilePersistencePort.ensureExists(userId, now);
        String jwtDisplayName = currentJwtDisplayName();
        if ("Learner".equals(profile.displayName()) && jwtDisplayName != null && !jwtDisplayName.isBlank()) {
            profile = learningProfilePersistencePort.updateDisplayName(userId, jwtDisplayName.trim(), now);
        }
        return ResponseEntity.ok(profile);
    }

    @PatchMapping
    public ResponseEntity<LearningProfile> updateProfile(@RequestBody UpdateProfileRequest request) {
        UUID userId = currentUserProvider.currentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        if (request == null || request.displayName() == null || request.displayName().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        String displayName = request.displayName().trim();
        if (displayName.length() > 255) {
            return ResponseEntity.badRequest().build();
        }
        LearningProfile updated = learningProfilePersistencePort.updateDisplayName(userId, displayName, Instant.now());
        return ResponseEntity.ok(updated);
    }

    public record UpdateProfileRequest(
            String displayName
    ) {
    }

    private String currentJwtDisplayName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            return jwt.getClaimAsString("name");
        }
        return null;
    }
}
