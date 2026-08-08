package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.MicroConceptGenerationJob;
import com.myproject.practico.domain.MicroConceptGenerationJobStatus;

import java.util.Optional;

public interface MicroConceptGenerationJobPersistencePort {

    Optional<MicroConceptGenerationJob> findActiveByProgramIdAndMicroConceptId(Long programId, Long microConceptId);

    Optional<MicroConceptGenerationJob> findLatestByProgramIdAndMicroConceptId(Long programId, Long microConceptId);

    MicroConceptGenerationJob create(
            Long programId,
            Long microConceptId,
            MicroConceptGenerationJobStatus status,
            Integer progressPercent,
            String statusMessage,
            String requestedBy
    );
}
