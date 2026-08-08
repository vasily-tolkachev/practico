package com.myproject.practico.application.port.in;

import com.myproject.practico.application.microconcept.MicroConceptGeneratedContentResult;

import java.util.Optional;

public interface GetMicroConceptGeneratedContentUseCase {

    Optional<MicroConceptGeneratedContentResult> getContent(Long programId, Long microConceptId);
}
