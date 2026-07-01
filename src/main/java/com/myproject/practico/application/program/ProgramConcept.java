package com.myproject.practico.application.program;

import java.util.List;

public record ProgramConcept(
        Long conceptId,
        String title,
        String description,
        Integer estimatedTimeMinutes,
        String difficulty,
        List<String> prerequisites,
        List<ProgramMicroConcept> microConcepts
) {
    public ProgramConcept {
        prerequisites = prerequisites == null ? List.of() : List.copyOf(prerequisites);
        microConcepts = microConcepts == null ? List.of() : List.copyOf(microConcepts);
    }
}
