package com.myproject.practico.application.learning.state;

public record CompletedActivity(
        ActivityType type,
        String summary
) implements LearningActivity {
}
