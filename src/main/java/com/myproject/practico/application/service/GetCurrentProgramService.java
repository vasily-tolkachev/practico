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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
                                .map(micro -> new ProgramMicroConcept(micro.id(), micro.name(), micro.sortOrder()))
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
}
