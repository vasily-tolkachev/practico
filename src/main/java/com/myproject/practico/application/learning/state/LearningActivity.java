package com.myproject.practico.application.learning.state;

public sealed interface LearningActivity permits QuestionActivity, LearningCardActivity, PracticeActivity, QuickCheckActivity, RetryActivity, CompletedActivity {
    ActivityType type();
}
