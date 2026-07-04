package com.myproject.practico.application.program;

import java.util.List;

public record GeneratedConceptStructure(
        String name,
        List<String> microConcepts
) {
    public GeneratedConceptStructure {
        microConcepts = microConcepts == null ? List.of() : List.copyOf(microConcepts);
    }
}
