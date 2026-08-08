package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.MicroConceptContent;
import com.myproject.practico.domain.MicroConceptContentStatus;

import java.time.Instant;
import java.util.Optional;

public interface MicroConceptContentPersistencePort {

    Optional<MicroConceptContent> findByProgramIdAndMicroConceptId(Long programId, Long microConceptId);

    MicroConceptContent upsert(
            Long programId,
            Long microConceptId,
            MicroConceptContentStatus status,
            String questionPayload,
            String learningCardPayload,
            String practicePayload,
            String quickCheckPayload,
            String retryPayload,
            Instant generatedAt
    );
}
