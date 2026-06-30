package com.myproject.practico.application.learning.state;

public record ProgressSnapshot(
        Integer conceptOrder,
        Integer totalConcepts,
        Integer microConceptOrder,
        Integer totalMicroConcepts,
        Integer answeredCount
) {
}
