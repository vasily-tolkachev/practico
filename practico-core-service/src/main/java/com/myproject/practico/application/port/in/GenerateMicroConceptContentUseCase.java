package com.myproject.practico.application.port.in;

import com.myproject.practico.application.microconcept.MicroConceptGenerationTriggerResult;

import java.util.Optional;

public interface GenerateMicroConceptContentUseCase {

    Optional<MicroConceptGenerationTriggerResult> generate(Long programId, Long microConceptId, String requestedBy);
}
