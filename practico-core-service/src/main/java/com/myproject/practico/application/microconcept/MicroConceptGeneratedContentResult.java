package com.myproject.practico.application.microconcept;

import java.time.Instant;

public record MicroConceptGeneratedContentResult(
        Long programId,
        Long microConceptId,
        String status,
        String questionPayload,
        String learningCardPayload,
        String practicePayload,
        String quickCheckPayload,
        String retryPayload,
        Instant generatedAt,
        Instant updatedAt
) {
}
