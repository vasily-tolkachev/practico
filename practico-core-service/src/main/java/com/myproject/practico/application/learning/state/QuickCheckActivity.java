package com.myproject.practico.application.learning.state;

public record QuickCheckActivity(
        ActivityType type,
        String question
) implements LearningActivity {
}
