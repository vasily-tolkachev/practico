package com.myproject.practico.application.service;

public record LearningInput(
        String rawText,
        PracticeAnswer practiceAnswer
) {
    public LearningInput {
        rawText = rawText == null ? "" : rawText;
        practiceAnswer = practiceAnswer == null ? new PracticeAnswer(null, java.util.Set.of()) : practiceAnswer;
    }
}
