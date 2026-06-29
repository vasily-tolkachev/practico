package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.Question;

import java.util.Optional;
import java.util.Set;

public interface GetQuestionUseCase {
    Optional<Question> getNext(Difficulty preferredDifficulty, Set<Long> excludedQuestionIds);

    Optional<Question> getNextInConcept(Long conceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds);

    Optional<Question> getNextInMicroConcept(Long conceptId, Long microConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds);

    Optional<Question> getNextFromNextMicroConcept(
            Long conceptId,
            Long currentMicroConceptId,
            Difficulty preferredDifficulty,
            Set<Long> excludedQuestionIds
    );

    Optional<Question> getNextFromNextConcept(Long currentConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds);

    Optional<Question> getById(Long id);

    int conceptOrder(Long conceptId);

    int totalConcepts();

    int microConceptOrder(Long conceptId, Long microConceptId);

    int totalMicroConcepts(Long conceptId);
}
