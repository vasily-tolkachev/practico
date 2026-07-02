package com.myproject.practico.application.program;

import java.util.List;

public record GeneratedTopicStructure(
        String name,
        List<GeneratedConceptStructure> concepts
) {
    public GeneratedTopicStructure {
        concepts = concepts == null ? List.of() : List.copyOf(concepts);
    }
}
