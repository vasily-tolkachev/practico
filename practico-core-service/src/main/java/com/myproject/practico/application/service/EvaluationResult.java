package com.myproject.practico.application.service;

import com.myproject.practico.domain.LearningCard;
import com.myproject.practico.domain.QuickCheck;

import java.util.List;

public record EvaluationResult(
        int score,
        boolean answeredQuestion,
        String evaluation,
        LearningCard learningCard,
        QuickCheck quickCheck,
        List<PracticeItem> practiceItems,
        List<String> retryRubric,
        String retryQuestion
) {}
