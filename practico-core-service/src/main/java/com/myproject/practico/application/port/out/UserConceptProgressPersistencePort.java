package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.UserConceptProgress;
import com.myproject.practico.domain.ProgressStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserConceptProgressPersistencePort {
    Optional<UserConceptProgress> findByProfileIdAndConceptId(UUID profileId, Long conceptId);

    UserConceptProgress upsert(
            UUID profileId,
            Long conceptId,
            ProgressStatus status,
            int correctAnswers,
            int totalAnswers,
            Instant updatedAt
    );
}
