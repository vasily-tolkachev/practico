package com.myproject.practico.application.service;

import java.util.List;
import java.util.Map;

public record PracticeItem(
        PracticeType type,
        String question,
        List<String> options,
        List<Integer> correctOptions,
        Boolean expectedBoolean,
        List<Integer> correctOrder,
        List<String> leftItems,
        List<String> rightItems,
        Map<Integer, Integer> correctMatches,
        Boolean ambiguousIndexing
) {
    public PracticeItem {
        options = options == null ? List.of() : List.copyOf(options);
        correctOptions = correctOptions == null ? List.of() : List.copyOf(correctOptions);
        correctOrder = correctOrder == null ? List.of() : List.copyOf(correctOrder);
        leftItems = leftItems == null ? List.of() : List.copyOf(leftItems);
        rightItems = rightItems == null ? List.of() : List.copyOf(rightItems);
        correctMatches = correctMatches == null ? Map.of() : Map.copyOf(correctMatches);
    }
}
