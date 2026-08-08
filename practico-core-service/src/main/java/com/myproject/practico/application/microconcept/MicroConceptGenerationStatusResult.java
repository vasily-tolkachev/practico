package com.myproject.practico.application.microconcept;

import java.time.Instant;

public record MicroConceptGenerationStatusResult(
        Long programId,
        Long microConceptId,
        Long jobId,
        String status,
        Integer progressPercent,
        String message,
        Instant updatedAt
) {
}
