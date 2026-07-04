package com.myproject.practico.application.program;

public record ProgramMicroConcept(
        Long microConceptId,
        String title,
        Integer sortOrder,
        boolean completed,
        boolean current,
        boolean locked
) {
}
