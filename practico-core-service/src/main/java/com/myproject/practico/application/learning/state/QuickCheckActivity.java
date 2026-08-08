package com.myproject.practico.application.learning.state;

import com.myproject.practico.application.service.PracticeType;

import java.util.List;

public record QuickCheckActivity(
        ActivityType type,
        PracticeType questionType,
        String question,
        List<String> options,
        List<String> leftItems,
        List<String> rightItems
) implements LearningActivity {
    public QuickCheckActivity {
        options = options == null ? List.of() : List.copyOf(options);
        leftItems = leftItems == null ? List.of() : List.copyOf(leftItems);
        rightItems = rightItems == null ? List.of() : List.copyOf(rightItems);
    }
}
