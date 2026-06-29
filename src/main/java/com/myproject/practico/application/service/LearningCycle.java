package com.myproject.practico.application.service;

import com.myproject.practico.domain.LearningCard;
import com.myproject.practico.domain.QuickCheck;

public record LearningCycle(
        LearningCard learningCard,
        QuickCheck quickCheck,
        String retryQuestion
) {
}
