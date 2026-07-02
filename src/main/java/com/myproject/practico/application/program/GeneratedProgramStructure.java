package com.myproject.practico.application.program;

import java.util.List;

public record GeneratedProgramStructure(
        List<GeneratedTopicStructure> topics
) {
    public GeneratedProgramStructure {
        topics = topics == null ? List.of() : List.copyOf(topics);
    }
}
