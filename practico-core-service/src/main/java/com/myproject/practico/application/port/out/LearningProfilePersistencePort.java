package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.LearningProfile;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface LearningProfilePersistencePort {
    Optional<LearningProfile> findById(UUID userId);
    LearningProfile ensureExists(UUID userId, Instant now);
    LearningProfile touch(UUID userId, Instant now);
}
