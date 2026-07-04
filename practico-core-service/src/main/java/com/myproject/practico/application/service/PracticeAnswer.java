package com.myproject.practico.application.service;

import java.util.Set;

public record PracticeAnswer(
        Boolean booleanAnswer,
        Set<Integer> selectedOptions
) {
    public PracticeAnswer {
        selectedOptions = selectedOptions == null ? Set.of() : Set.copyOf(selectedOptions);
    }
}
