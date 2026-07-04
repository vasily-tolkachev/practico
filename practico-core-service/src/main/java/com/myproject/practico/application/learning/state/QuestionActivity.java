package com.myproject.practico.application.learning.state;

public record QuestionActivity(
        ActivityType type,
        Long questionId,
        String text,
        String difficulty,
        String questionType
) implements LearningActivity {
}
