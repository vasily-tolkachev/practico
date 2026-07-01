package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetCurrentProgramUseCase;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.application.program.LearningProgram;
import com.myproject.practico.application.program.ProgramConcept;
import com.myproject.practico.application.program.ProgramMicroConcept;
import com.myproject.practico.application.program.ProgramProgress;
import com.myproject.practico.domain.Concept;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.Topic;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class GetCurrentProgramService implements GetCurrentProgramUseCase {

    private static final String EMPTY_PROGRAM_ID = "empty";
    private static final String EMPTY_PROGRAM_TITLE = "Learning Program";
    private static final String EMPTY_GOAL_TITLE = "Start learning to build a program";

    private final QuestionPersistencePort questionPersistencePort;
    private final LearningSessionService learningSessionService;
    private final GetQuestionUseCase getQuestionUseCase;

    public GetCurrentProgramService(
            QuestionPersistencePort questionPersistencePort,
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase
    ) {
        this.questionPersistencePort = questionPersistencePort;
        this.learningSessionService = learningSessionService;
        this.getQuestionUseCase = getQuestionUseCase;
    }

    @Override
    public LearningProgram getCurrentProgram(String userId) {
        List<Question> questions = questionPersistencePort.findAll();
        if (questions.isEmpty()) {
            return new LearningProgram(
                    EMPTY_PROGRAM_ID,
                    EMPTY_PROGRAM_TITLE,
                    EMPTY_GOAL_TITLE,
                    List.of(),
                    new ProgramProgress(0, 0)
            );
        }

        Map<Long, List<Question>> questionsByTopic = questions.stream()
                .filter(question -> question.concept() != null && question.concept().topic() != null)
                .collect(java.util.stream.Collectors.groupingBy(question -> question.concept().topic().id()));

        if (questionsByTopic.isEmpty()) {
            return new LearningProgram(
                    EMPTY_PROGRAM_ID,
                    EMPTY_PROGRAM_TITLE,
                    EMPTY_GOAL_TITLE,
                    List.of(),
                    new ProgramProgress(0, 0)
            );
        }

        Long currentTopicId = resolveCurrentTopicId(userId);
        Long targetTopicId = questionsByTopic.containsKey(currentTopicId)
                ? currentTopicId
                : questionsByTopic.keySet().stream().filter(Objects::nonNull).sorted().findFirst().orElse(null);
        List<Question> topicQuestions = targetTopicId == null ? List.of() : questionsByTopic.get(targetTopicId);
        if (topicQuestions == null || topicQuestions.isEmpty()) {
            return new LearningProgram(
                    EMPTY_PROGRAM_ID,
                    EMPTY_PROGRAM_TITLE,
                    EMPTY_GOAL_TITLE,
                    List.of(),
                    new ProgramProgress(0, 0)
            );
        }

        Topic topic = topicQuestions.stream()
                .map(Question::concept)
                .filter(Objects::nonNull)
                .map(Concept::topic)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);

        List<com.myproject.practico.domain.MicroConcept> orderedTopicMicroConcepts = topicQuestions.stream()
                .map(Question::microConcept)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        com.myproject.practico.domain.MicroConcept::id,
                        micro -> micro,
                        (left, right) -> left
                ))
                .values()
                .stream()
                .sorted(Comparator
                        .comparing((com.myproject.practico.domain.MicroConcept micro) ->
                                micro.sortOrder() == null ? Integer.MAX_VALUE : micro.sortOrder())
                        .thenComparing(com.myproject.practico.domain.MicroConcept::id, Comparator.nullsLast(Long::compareTo)))
                .toList();

        ProgramMicroConceptStatusIndex statusIndex = buildStatusIndex(userId, orderedTopicMicroConcepts);

        List<ProgramConcept> concepts = topicQuestions.stream()
                .map(Question::concept)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toMap(
                        Concept::id,
                        concept -> concept,
                        (left, right) -> left
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(Concept::id, Comparator.nullsLast(Long::compareTo)))
                .map(concept -> new ProgramConcept(
                        concept.id(),
                        concept.name(),
                        topicQuestions.stream()
                                .map(Question::microConcept)
                                .filter(Objects::nonNull)
                                .filter(micro -> micro.concept() != null && Objects.equals(micro.concept().id(), concept.id()))
                                .collect(java.util.stream.Collectors.toMap(
                                        micro -> micro.id(),
                                        micro -> micro,
                                        (left, right) -> left
                                ))
                                .values()
                                .stream()
                                .sorted(Comparator
                                        .comparing((com.myproject.practico.domain.MicroConcept micro) ->
                                                micro.sortOrder() == null ? Integer.MAX_VALUE : micro.sortOrder())
                                        .thenComparing(com.myproject.practico.domain.MicroConcept::id, Comparator.nullsLast(Long::compareTo)))
                                .map(micro -> {
                                    Long microId = micro.id();
                                    boolean current = microId != null && Objects.equals(statusIndex.currentMicroConceptId(), microId);
                                    boolean completed = microId != null
                                            && statusIndex.completedMicroConceptIds().contains(microId)
                                            && !current;
                                    boolean locked = !completed && !current;
                                    return new ProgramMicroConcept(
                                            microId,
                                            micro.name(),
                                            micro.sortOrder(),
                                            completed,
                                            current,
                                            locked
                                    );
                                })
                                .toList()
                ))
                .toList();

        int totalMicroConcepts = concepts.stream()
                .mapToInt(concept -> concept.microConcepts().size())
                .sum();
        String topicName = topic == null || topic.name() == null ? "General" : topic.name();

        return new LearningProgram(
                "topic-" + (targetTopicId == null ? "unknown" : targetTopicId),
                topicName + " Program",
                "Master " + topicName,
                concepts,
                new ProgramProgress(concepts.size(), totalMicroConcepts)
        );
    }

    private Long resolveCurrentTopicId(String userId) {
        Optional<LearningSessionStore.LearningSession> session = learningSessionService.getSession(userId);
        if (session.isEmpty() || session.get().currentQuestionId() == null) {
            return null;
        }

        return getQuestionUseCase.getById(session.get().currentQuestionId())
                .map(Question::concept)
                .filter(Objects::nonNull)
                .map(Concept::topic)
                .filter(Objects::nonNull)
                .map(Topic::id)
                .orElse(null);
    }

    private ProgramMicroConceptStatusIndex buildStatusIndex(
            String userId,
            List<com.myproject.practico.domain.MicroConcept> orderedTopicMicroConcepts
    ) {
        Optional<LearningSessionStore.LearningSession> sessionOptional = learningSessionService.getSession(userId);
        if (sessionOptional.isEmpty() || orderedTopicMicroConcepts.isEmpty()) {
            return new ProgramMicroConceptStatusIndex(Set.of(), null);
        }

        LearningSessionStore.LearningSession session = sessionOptional.get();
        Set<Long> completed = new HashSet<>(session.masteredMicroConceptIds());
        Long currentFromQuestion = resolveCurrentMicroConceptId(session);

        Long current;
        if (currentFromQuestion != null) {
            current = currentFromQuestion;
        } else {
            current = orderedTopicMicroConcepts.stream()
                    .map(com.myproject.practico.domain.MicroConcept::id)
                    .filter(Objects::nonNull)
                    .filter(id -> !completed.contains(id))
                    .findFirst()
                    .orElse(null);
        }

        return new ProgramMicroConceptStatusIndex(Set.copyOf(completed), current);
    }

    private Long resolveCurrentMicroConceptId(LearningSessionStore.LearningSession session) {
        if (session.currentQuestionId() == null) {
            return null;
        }

        return getQuestionUseCase.getById(session.currentQuestionId())
                .map(Question::microConcept)
                .filter(Objects::nonNull)
                .map(com.myproject.practico.domain.MicroConcept::id)
                .orElse(null);
    }

    private record ProgramMicroConceptStatusIndex(
            Set<Long> completedMicroConceptIds,
            Long currentMicroConceptId
    ) {
    }
}
