package com.myproject.practico.application.program;

import java.util.List;

public record ProgramConcept(
        Long conceptId,
        String title,
        List<ProgramMicroConcept> microConcepts
) {
    public ProgramConcept {
        microConcepts = microConcepts == null ? List.of() : List.copyOf(microConcepts);
    }
}
