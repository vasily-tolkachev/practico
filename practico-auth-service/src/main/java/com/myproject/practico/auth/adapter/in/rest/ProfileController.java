package com.myproject.practico.auth.adapter.in.rest;

import com.myproject.practico.auth.adapter.in.auth.JwtPrincipal;
import com.myproject.practico.auth.adapter.in.rest.dto.UpdateProfileRequest;
import com.myproject.practico.auth.application.dto.UserProfileResponse;
import com.myproject.practico.auth.application.port.GetUserProfileUseCase;
import com.myproject.practico.auth.application.port.UpdateUserProfileUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;

    public ProfileController(
            GetUserProfileUseCase getUserProfileUseCase,
            UpdateUserProfileUseCase updateUserProfileUseCase
    ) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> getProfile() {
        UUID userId = currentUserId();
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        return getUserProfileUseCase.getByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping
    public ResponseEntity<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        UUID userId = currentUserId();
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
        return updateUserProfileUseCase.updateDisplayName(userId, displayName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return jwtPrincipal.userId();
        }
        return null;
    }
}
