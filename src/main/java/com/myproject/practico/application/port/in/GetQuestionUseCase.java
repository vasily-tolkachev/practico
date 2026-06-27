package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.Question;

import java.util.Optional;
import java.util.Set;

public interface GetQuestionUseCase {
    Optional<Question> getNext(String preferredDifficulty, Set<Long> excludedQuestionIds);

    Optional<Question> getNextInConcept(Long conceptId, String preferredDifficulty, Set<Long> excludedQuestionIds);

    Optional<Question> getNextFromNextConcept(Long currentConceptId, String preferredDifficulty, Set<Long> excludedQuestionIds);

    Optional<Question> getById(Long id);
}
