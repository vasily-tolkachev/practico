package com.myproject.practico.application.learning.state;

public record CompletedActivity(
        String summary
) implements LearningActivity {
    @Override
    public ActivityType type() {
        return ActivityType.COMPLETED;
    }
}
