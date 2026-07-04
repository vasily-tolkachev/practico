package com.myproject.practico.application.program;

import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.QuestionType;

public record GeneratedQuestion(
        String text,
        String expectedAnswer,
        String explanation,
        Difficulty difficulty,
        QuestionType questionType
) {
}
