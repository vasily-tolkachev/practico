package com.myproject.practico.application.learning.state;

public record QuestionActivity(
        Long questionId,
        String text,
        String difficulty,
        String questionType
) implements LearningActivity {
}
