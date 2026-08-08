package com.myproject.practico.application.microconcept;

import com.myproject.practico.domain.MicroConceptGenerationJobStatus;

public record MicroConceptGenerationTriggerResult(
        Long jobId,
        Long programId,
        Long microConceptId,
        MicroConceptGenerationJobStatus status,
        Integer progressPercent,
        String message
) {
}
