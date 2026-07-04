package com.myproject.practico.application.service;

import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.UserConceptProgress;

public record LearningResult(
        EvaluationResult evaluation,
        UserConceptProgress conceptProgress,
        LearningPhase nextPhase,
        Question nextQuestion
) {}
