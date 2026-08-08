package com.myproject.practico.domain;

import java.time.Instant;

public record MicroConceptContent(
        Long id,
        Long programId,
        Long microConceptId,
        MicroConceptContentStatus status,
        String questionPayload,
        String learningCardPayload,
        String practicePayload,
        String quickCheckPayload,
        String retryPayload,
        Instant generatedAt,
        Instant updatedAt
) {
}
