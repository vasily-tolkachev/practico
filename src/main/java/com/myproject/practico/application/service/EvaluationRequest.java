package com.myproject.practico.application.service;

import com.myproject.practico.domain.QuestionType;

public record EvaluationRequest(
        String question,
        String answer,
        QuestionType questionType
) {}
