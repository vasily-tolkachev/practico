package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.domain.Question;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class GetQuestionService implements GetQuestionUseCase {

    private final QuestionPersistencePort questionPersistencePort;

    public GetQuestionService(QuestionPersistencePort questionPersistencePort) {
        this.questionPersistencePort = questionPersistencePort;
    }

    @Override
    public Optional<Question> getNext(String preferredDifficulty, Set<Long> excludedQuestionIds) {
        List<Question> availableQuestions = questionPersistencePort.findAll().stream()
                .filter(question -> excludedQuestionIds == null || !excludedQuestionIds.contains(question.id()))
                .toList();

        if (availableQuestions.isEmpty()) {
            return Optional.empty();
        }

        List<Question> preferredQuestions = availableQuestions.stream()
                .filter(question -> matchesDifficulty(question.difficulty(), preferredDifficulty))
                .toList();

        if (!preferredQuestions.isEmpty()) {
            return Optional.of(randomFrom(preferredQuestions));
        }

        return Optional.of(randomFrom(availableQuestions));
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

    private boolean matchesDifficulty(String questionDifficulty, String preferredDifficulty) {
        if (preferredDifficulty == null || preferredDifficulty.isBlank()) {
            return true;
        }

        if (questionDifficulty == null || questionDifficulty.isBlank()) {
            return false;
        }

        String normalizedPreferred = preferredDifficulty.toLowerCase();
        String normalizedQuestionDifficulty = questionDifficulty.toLowerCase();

        if ("easy".equals(normalizedPreferred)) {
            return normalizedQuestionDifficulty.contains("easy")
                    || normalizedQuestionDifficulty.contains("basic")
                    || normalizedQuestionDifficulty.contains("junior");
        }

        if ("medium".equals(normalizedPreferred)) {
            return normalizedQuestionDifficulty.contains("medium")
                    || normalizedQuestionDifficulty.contains("middle")
                    || normalizedQuestionDifficulty.contains("mid");
        }

        return normalizedQuestionDifficulty.contains("hard")
                || normalizedQuestionDifficulty.contains("advanced")
                || normalizedQuestionDifficulty.contains("senior");
    }
}
