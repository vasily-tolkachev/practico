package com.myproject.practico.application.service;

import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.UserConceptProgress;

public record LearningResult(
        AiResponse aiResponse,
        UserConceptProgress conceptProgress,
        Question nextQuestion
) {}
