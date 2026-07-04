package com.myproject.practico.application.program;

import java.util.List;

public record LearningProgram(
        String programId,
        Long goalId,
        ProgramOrigin origin,
        String title,
        String goalTitle,
        List<ProgramConcept> concepts,
        ProgramProgress progress
) {
    public LearningProgram {
        concepts = concepts == null ? List.of() : List.copyOf(concepts);
    }
}
