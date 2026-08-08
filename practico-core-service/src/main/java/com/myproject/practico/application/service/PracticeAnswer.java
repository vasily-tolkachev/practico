package com.myproject.practico.application.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record PracticeAnswer(
        Boolean booleanAnswer,
        Set<Integer> selectedOptions,
        List<Integer> orderedOptions,
        Map<Integer, Integer> matches
) {
    public PracticeAnswer {
        selectedOptions = selectedOptions == null ? Set.of() : Set.copyOf(selectedOptions);
        orderedOptions = orderedOptions == null ? List.of() : List.copyOf(orderedOptions);
        matches = matches == null ? Map.of() : Map.copyOf(matches);
    }
}
