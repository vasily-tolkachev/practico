package com.myproject.practico.application.learning.state;

public record LearningContext(
        Long topicId,
        String topicName,
        Long conceptId,
        String conceptName,
        Long microConceptId,
        String microConceptName
) {
}
