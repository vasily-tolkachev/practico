package com.myproject.practico.application.service;

import com.myproject.practico.domain.QuestionType;

import java.util.List;

public record EvaluationRequest(
        String question,
        String answer,
        QuestionType questionType,
        List<String> retryRubric
) {}
