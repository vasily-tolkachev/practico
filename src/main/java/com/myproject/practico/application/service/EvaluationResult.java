package com.myproject.practico.application.service;

import com.myproject.practico.domain.LearningCard;

public record EvaluationResult(
        int score,
        String evaluation,
        LearningCard learningCard
) {}
