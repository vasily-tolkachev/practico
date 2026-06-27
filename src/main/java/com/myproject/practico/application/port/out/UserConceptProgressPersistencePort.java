package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.UserConceptProgress;
import com.myproject.practico.domain.ProgressStatus;

import java.time.Instant;
import java.util.Optional;

public interface UserConceptProgressPersistencePort {
    Optional<UserConceptProgress> findByUserIdAndConceptId(Long userId, Long conceptId);

    UserConceptProgress upsert(
            Long userId,
            Long conceptId,
            ProgressStatus status,
            int correctAnswers,
            int totalAnswers,
            Instant updatedAt
    );
}
