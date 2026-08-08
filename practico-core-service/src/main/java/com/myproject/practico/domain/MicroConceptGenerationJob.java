package com.myproject.practico.domain;

import java.time.Instant;

public record MicroConceptGenerationJob(
        Long id,
        Long programId,
        Long microConceptId,
        MicroConceptGenerationJobStatus status,
        Integer progressPercent,
        String statusMessage,
        String requestedBy,
        Instant createdAt,
        Instant updatedAt
) {
}
