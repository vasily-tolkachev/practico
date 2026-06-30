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
    public Optional<Question> getNextInMicroConcept(Long conceptId, Long microConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) {
        if (conceptId == null || microConceptId == null) {
            return Optional.empty();
        }

        List<Question> questions = filterExcluded(questionPersistencePort.findAll(), excludedQuestionIds).stream()
                .filter(question -> question.concept() != null && Objects.equals(question.concept().id(), conceptId))
                .filter(question -> question.microConcept() != null && Objects.equals(question.microConcept().id(), microConceptId))
                .toList();

        return pickByDifficulty(questions, preferredDifficulty);
    }

    @Override
    public Optional<Question> getNextFromNextMicroConcept(
            Long conceptId,
            Long currentMicroConceptId,
            Difficulty preferredDifficulty,
            Set<Long> excludedQuestionIds
    ) {
        if (conceptId == null) {
            return Optional.empty();
        }

        List<Question> allQuestionsInConcept = questionPersistencePort.findAll().stream()
                .filter(question -> question.concept() != null && Objects.equals(question.concept().id(), conceptId))
                .filter(question -> question.microConcept() != null && question.microConcept().id() != null)
                .toList();
        if (allQuestionsInConcept.isEmpty()) {
            return Optional.empty();
        }

        List<Long> orderedMicroConceptIds = allQuestionsInConcept.stream()
                .map(Question::microConcept)
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing((com.myproject.practico.domain.MicroConcept micro) ->
                                micro.sortOrder() == null ? Integer.MAX_VALUE : micro.sortOrder())
                        .thenComparing(com.myproject.practico.domain.MicroConcept::id))
                .map(com.myproject.practico.domain.MicroConcept::id)
                .distinct()
                .toList();

        if (orderedMicroConceptIds.isEmpty()) {
            return Optional.empty();
        }

        Long targetMicroConceptId;
        if (currentMicroConceptId == null) {
            targetMicroConceptId = orderedMicroConceptIds.get(0);
        } else {
            int currentIndex = orderedMicroConceptIds.indexOf(currentMicroConceptId);
            if (currentIndex < 0) {
                targetMicroConceptId = orderedMicroConceptIds.get(0);
            } else if (currentIndex + 1 >= orderedMicroConceptIds.size()) {
                return Optional.empty();
            } else {
                targetMicroConceptId = orderedMicroConceptIds.get(currentIndex + 1);
            }
        }

        List<Question> questions = filterExcluded(allQuestionsInConcept, excludedQuestionIds).stream()
                .filter(question -> question.microConcept() != null && Objects.equals(question.microConcept().id(), targetMicroConceptId))
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
                .orElse(null);

        if (targetConceptId == null) {
            return Optional.empty();
        }

        List<Question> questions = availableQuestions.stream()
                .filter(question -> question.concept() != null && Objects.equals(question.concept().id(), targetConceptId))
                .toList();
        if (questions.isEmpty()) {
            return Optional.empty();
        }

        List<Long> orderedMicroConceptIds = questions.stream()
                .map(Question::microConcept)
                .filter(Objects::nonNull)
                .filter(micro -> micro.id() != null)
                .sorted(Comparator
                        .comparing((com.myproject.practico.domain.MicroConcept micro) ->
                                micro.sortOrder() == null ? Integer.MAX_VALUE : micro.sortOrder())
                        .thenComparing(com.myproject.practico.domain.MicroConcept::id))
                .map(com.myproject.practico.domain.MicroConcept::id)
                .distinct()
                .toList();
        if (orderedMicroConceptIds.isEmpty()) {
            return pickByDifficulty(questions, preferredDifficulty);
        }

        Long firstMicroConceptId = orderedMicroConceptIds.get(0);
        List<Question> firstMicroConceptQuestions = questions.stream()
                .filter(question -> question.microConcept() != null && Objects.equals(question.microConcept().id(), firstMicroConceptId))
                .toList();
        return pickByDifficulty(firstMicroConceptQuestions, preferredDifficulty);
    }

    @Override
    public Optional<Question> getFirstFromTopic(String topicName, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) {
        if (topicName == null || topicName.isBlank()) {
            return Optional.empty();
        }

        List<Question> topicQuestions = filterExcluded(questionPersistencePort.findAll(), excludedQuestionIds).stream()
                .filter(question -> question.concept() != null
                        && question.concept().topic() != null
                        && question.concept().topic().name() != null
                        && question.concept().topic().name().equalsIgnoreCase(topicName))
                .toList();
        if (topicQuestions.isEmpty()) {
            return Optional.empty();
        }

        List<Long> orderedConceptIds = topicQuestions.stream()
                .map(question -> question.concept() == null ? null : question.concept().id())
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
        if (orderedConceptIds.isEmpty()) {
            return Optional.empty();
        }

        Long firstConceptId = orderedConceptIds.get(0);
        List<Question> firstConceptQuestions = topicQuestions.stream()
                .filter(question -> question.concept() != null && Objects.equals(question.concept().id(), firstConceptId))
                .toList();
        if (firstConceptQuestions.isEmpty()) {
            return Optional.empty();
        }

        List<Long> orderedMicroConceptIds = firstConceptQuestions.stream()
                .map(Question::microConcept)
                .filter(Objects::nonNull)
                .filter(micro -> micro.id() != null)
                .sorted(Comparator
                        .comparing((com.myproject.practico.domain.MicroConcept micro) ->
                                micro.sortOrder() == null ? Integer.MAX_VALUE : micro.sortOrder())
                        .thenComparing(com.myproject.practico.domain.MicroConcept::id))
                .map(com.myproject.practico.domain.MicroConcept::id)
                .distinct()
                .toList();
        if (orderedMicroConceptIds.isEmpty()) {
            return pickByDifficulty(firstConceptQuestions, preferredDifficulty);
        }

        Long firstMicroConceptId = orderedMicroConceptIds.get(0);
        List<Question> firstMicroQuestions = firstConceptQuestions.stream()
                .filter(question -> question.microConcept() != null && Objects.equals(question.microConcept().id(), firstMicroConceptId))
                .toList();
        return pickByDifficulty(firstMicroQuestions, preferredDifficulty);
    }

    @Override
    public Optional<Question> getFirstFromConceptName(String conceptName, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) {
        if (conceptName == null || conceptName.isBlank()) {
            return Optional.empty();
        }

        List<Question> conceptQuestions = filterExcluded(questionPersistencePort.findAll(), excludedQuestionIds).stream()
                .filter(question -> question.concept() != null
                        && question.concept().name() != null
                        && question.concept().name().equalsIgnoreCase(conceptName))
                .toList();
        if (conceptQuestions.isEmpty()) {
            return Optional.empty();
        }

        List<Long> orderedMicroConceptIds = conceptQuestions.stream()
                .map(Question::microConcept)
                .filter(Objects::nonNull)
                .filter(micro -> micro.id() != null)
                .sorted(Comparator
                        .comparing((com.myproject.practico.domain.MicroConcept micro) ->
                                micro.sortOrder() == null ? Integer.MAX_VALUE : micro.sortOrder())
                        .thenComparing(com.myproject.practico.domain.MicroConcept::id))
                .map(com.myproject.practico.domain.MicroConcept::id)
                .distinct()
                .toList();
        if (orderedMicroConceptIds.isEmpty()) {
            return pickByDifficulty(conceptQuestions, preferredDifficulty);
        }

        Long firstMicroConceptId = orderedMicroConceptIds.get(0);
        List<Question> firstMicroQuestions = conceptQuestions.stream()
                .filter(question -> question.microConcept() != null && Objects.equals(question.microConcept().id(), firstMicroConceptId))
                .toList();
        return pickByDifficulty(firstMicroQuestions, preferredDifficulty);
    }

    @Override
    public Optional<Question> getById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return questionPersistencePort.findById(id);
    }

    @Override
    public int conceptOrder(Long conceptId) {
        if (conceptId == null) {
            return 0;
        }

        List<Long> orderedConceptIds = questionPersistencePort.findAll().stream()
                .map(question -> question.concept() == null ? null : question.concept().id())
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        int index = orderedConceptIds.indexOf(conceptId);
        return index < 0 ? 0 : index + 1;
    }

    @Override
    public int totalConcepts() {
        return (int) questionPersistencePort.findAll().stream()
                .map(question -> question.concept() == null ? null : question.concept().id())
                .filter(Objects::nonNull)
                .distinct()
                .count();
    }

    @Override
    public int microConceptOrder(Long conceptId, Long microConceptId) {
        if (conceptId == null || microConceptId == null) {
            return 0;
        }

        List<Long> orderedMicroConceptIds = orderedMicroConceptIds(conceptId);
        int index = orderedMicroConceptIds.indexOf(microConceptId);
        return index < 0 ? 0 : index + 1;
    }

    @Override
    public int totalMicroConcepts(Long conceptId) {
        if (conceptId == null) {
            return 0;
        }
        return orderedMicroConceptIds(conceptId).size();
    }

    private List<Long> orderedMicroConceptIds(Long conceptId) {
        return questionPersistencePort.findAll().stream()
                .filter(question -> question.concept() != null && Objects.equals(question.concept().id(), conceptId))
                .map(Question::microConcept)
                .filter(Objects::nonNull)
                .filter(micro -> micro.id() != null)
                .sorted(Comparator
                        .comparing((com.myproject.practico.domain.MicroConcept micro) ->
                                micro.sortOrder() == null ? Integer.MAX_VALUE : micro.sortOrder())
                        .thenComparing(com.myproject.practico.domain.MicroConcept::id))
                .map(com.myproject.practico.domain.MicroConcept::id)
                .distinct()
                .toList();
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
