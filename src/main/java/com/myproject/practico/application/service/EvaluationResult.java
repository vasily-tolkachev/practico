package com.myproject.practico.application.service;

import com.myproject.practico.domain.LearningCard;
import com.myproject.practico.domain.QuickCheck;

public record EvaluationResult(
        int score,
        String evaluation,
        LearningCard learningCard,
        QuickCheck quickCheck
) {}
