package com.myproject.practico.application.learning.state;

public record LearningCardActivity(
        ActivityType type,
        String title,
        String explanation
) implements LearningActivity {
}
