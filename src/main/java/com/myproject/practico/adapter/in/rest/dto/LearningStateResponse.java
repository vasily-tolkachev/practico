package com.myproject.practico.adapter.in.rest.dto;

import com.myproject.practico.application.service.LearningPhase;
import com.myproject.practico.application.service.PracticeType;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.QuestionType;

import java.util.List;

public record LearningStateResponse(
        boolean active,
        String userId,
        LearningPhase phase,
        TopicView topic,
        ConceptView concept,
        MicroConceptView microConcept,
        ProgressView progress,
        QuestionView question,
        LearningCardView learningCard,
        QuickCheckView quickCheck,
        PracticeView practice,
        RetryView retry
) {
    public record TopicView(
            Long id,
            String name
    ) {}

    public record ConceptView(
            Long id,
            String name
    ) {}

    public record MicroConceptView(
            Long id,
            String name,
            Integer sortOrder
    ) {}

    public record ProgressView(
            Integer conceptOrder,
            Integer totalConcepts,
            Integer microConceptOrder,
            Integer totalMicroConcepts,
            Integer answeredCount
    ) {}

    public record QuestionView(
            Long id,
            String text,
            Difficulty difficulty,
            QuestionType questionType
    ) {}

    public record LearningCardView(
            String title,
            String explanation
    ) {}

    public record QuickCheckView(
            String question,
            String expectedAnswer
    ) {}

    public record PracticeView(
            Integer currentIndex,
            Integer totalItems,
            List<PracticeItemView> items
    ) {}

    public record PracticeItemView(
            PracticeType type,
            String question,
            List<String> options
    ) {}

    public record RetryView(
            String question,
            List<String> rubric
    ) {}
}
