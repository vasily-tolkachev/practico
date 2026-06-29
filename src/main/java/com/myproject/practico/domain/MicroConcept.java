package com.myproject.practico.domain;

public record MicroConcept(
        Long id,
        Concept concept,
        String name,
        Integer sortOrder
) {
}
