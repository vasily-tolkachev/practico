package com.myproject.practico.application.port.in;

import com.myproject.practico.application.microconcept.MicroConceptGenerationStatusResult;

import java.util.Optional;

public interface GetMicroConceptGenerationStatusUseCase {

    Optional<MicroConceptGenerationStatusResult> getStatus(Long programId, Long microConceptId);
}
