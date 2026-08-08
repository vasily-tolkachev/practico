package com.myproject.practico.application.service;

import com.myproject.practico.domain.LearningCard;
import com.myproject.practico.domain.QuickCheck;

import java.util.List;

public record LearningCycle(
        LearningCard learningCard,
        QuickCheck quickCheck,
        PracticeItem quickCheckItem,
        List<PracticeItem> practiceItems,
        List<String> retryRubric,
        String retryQuestion,
        PracticeItem retryItem
) {
}
