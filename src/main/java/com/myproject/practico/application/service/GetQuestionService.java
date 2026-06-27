package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.Question;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class GetQuestionService implements GetQuestionUseCase {

    private final QuestionPersistencePort questionPersistencePort;

    public GetQuestionService(QuestionPersistencePort questionPersistencePort) {
        this.questionPersistencePort = questionPersistencePort;
    }

    @Override
    public Optional<Question> getNext(Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) {
        List<Question> availableQuestions = filterExcluded(questionPersistencePort.findAll(), excludedQuestionIds);
        return pickByDifficulty(availableQuestions, preferredDifficulty);
    }

    @Override
    public Optional<Question> getNextInConcept(Long conceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) {
        if (conceptId == null) {
            return Optional.empty();
        }

        List<Question> questions = filterExcluded(questionPersistencePort.findAll(), excludedQuestionIds).stream()
                .filter(question -> question.concept() != null && Objects.equals(question.concept().id(), conceptId))
                .toList();

        return pickByDifficulty(questions, preferredDifficulty);
    }

    @Override
    public Optional<Question> getNextFromNextConcept(Long currentConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) {
        List<Question> availableQuestions = filterExcluded(questionPersistencePort.findAll(), excludedQuestionIds);
        if (availableQuestions.isEmpty()) {
            return Optional.empty();
        }

        List<Long> orderedConceptIds = availableQuestions.stream()
                .map(question -> question.concept() == null ? null : question.concept().id())
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        if (orderedConceptIds.isEmpty()) {
            return Optional.empty();
        }

        Long targetConceptId = orderedConceptIds.stream()
                .filter(id -> currentConceptId == null || id > currentConceptId)
                .findFirst()
                .orElse(orderedConceptIds.get(0));

        List<Question> questions = availableQuestions.stream()
                .filter(question -> question.concept() != null && Objects.equals(question.concept().id(), targetConceptId))
                .toList();

        return pickByDifficulty(questions, preferredDifficulty);
    }

    @Override
    public Optional<Question> getById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return questionPersistencePort.findById(id);
    }

    private Question randomFrom(List<Question> questions) {
        List<Question> copy = new ArrayList<>(questions);
        return copy.get(ThreadLocalRandom.current().nextInt(copy.size()));
    }

    private List<Question> filterExcluded(List<Question> questions, Set<Long> excludedQuestionIds) {
        return questions.stream()
                .filter(question -> excludedQuestionIds == null || !excludedQuestionIds.contains(question.id()))
                .toList();
    }

    private Optional<Question> pickByDifficulty(List<Question> questions, Difficulty preferredDifficulty) {
        if (questions.isEmpty()) {
            return Optional.empty();
        }

        List<Question> preferredQuestions = questions.stream()
                .filter(question -> matchesDifficulty(question.difficulty(), preferredDifficulty))
                .toList();

        if (!preferredQuestions.isEmpty()) {
            return Optional.of(randomFrom(preferredQuestions));
        }

        return Optional.of(randomFrom(questions));
    }

    private boolean matchesDifficulty(Difficulty questionDifficulty, Difficulty preferredDifficulty) {
        if (preferredDifficulty == null) {
            return true;
        }

        if (questionDifficulty == null) {
            return false;
        }

        return questionDifficulty == preferredDifficulty;
    }
}
