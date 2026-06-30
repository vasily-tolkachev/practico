package com.myproject.practico.application.learning.state;

import java.util.List;

public record RetryActivity(
        String question,
        List<String> rubric
) implements LearningActivity {
    public RetryActivity {
        rubric = rubric == null ? List.of() : List.copyOf(rubric);
    }
}
