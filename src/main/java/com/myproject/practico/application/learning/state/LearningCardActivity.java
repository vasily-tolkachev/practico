package com.myproject.practico.application.learning.state;

public record LearningCardActivity(
        String title,
        String explanation
) implements LearningActivity {
}
