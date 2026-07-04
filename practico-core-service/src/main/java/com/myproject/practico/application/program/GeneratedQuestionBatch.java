package com.myproject.practico.application.program;

import java.util.List;

public record GeneratedQuestionBatch(
        List<GeneratedQuestion> questions,
        Long totalTokens,
        Double estimatedCostUsd
) {
    public GeneratedQuestionBatch {
        questions = questions == null ? List.of() : List.copyOf(questions);
    }
}
