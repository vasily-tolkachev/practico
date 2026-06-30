package com.myproject.practico.application.learning.state;

public record QuickCheckActivity(
        String question
) implements LearningActivity {
    @Override
    public ActivityType type() {
        return ActivityType.QUICK_CHECK;
    }
}
