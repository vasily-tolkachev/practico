package com.myproject.practico.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.practico.application.microconcept.MicroConceptGenerationTriggerResult;
import com.myproject.practico.application.port.in.GenerateMicroConceptContentUseCase;
import com.myproject.practico.application.port.out.AiQuestionGeneratorPort;
import com.myproject.practico.application.port.out.GeneratedQuestionPersistencePort;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptGenerationJobPersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.application.program.GeneratedQuestion;
import com.myproject.practico.application.program.GeneratedQuestionBatch;
import com.myproject.practico.application.program.ProgramMicroConceptTarget;
import com.myproject.practico.domain.MicroConceptContentStatus;
import com.myproject.practico.domain.MicroConceptGenerationJob;
import com.myproject.practico.domain.MicroConceptGenerationJobStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

public class GenerateMicroConceptContentService implements GenerateMicroConceptContentUseCase {

    private final LearningProgramPersistencePort learningProgramPersistencePort;
    private final ProgramMicroConceptReadPort programMicroConceptReadPort;
    private final MicroConceptContentPersistencePort microConceptContentPersistencePort;
    private final MicroConceptGenerationJobPersistencePort microConceptGenerationJobPersistencePort;
    private final AiQuestionGeneratorPort aiQuestionGeneratorPort;
    private final GeneratedQuestionPersistencePort generatedQuestionPersistencePort;
    private final ObjectMapper objectMapper;
    private final Executor microConceptGenerationExecutor;

    public GenerateMicroConceptContentService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            MicroConceptGenerationJobPersistencePort microConceptGenerationJobPersistencePort,
            AiQuestionGeneratorPort aiQuestionGeneratorPort,
            GeneratedQuestionPersistencePort generatedQuestionPersistencePort,
            ObjectMapper objectMapper,
            Executor microConceptGenerationExecutor
    ) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.microConceptContentPersistencePort = microConceptContentPersistencePort;
        this.microConceptGenerationJobPersistencePort = microConceptGenerationJobPersistencePort;
        this.aiQuestionGeneratorPort = aiQuestionGeneratorPort;
        this.generatedQuestionPersistencePort = generatedQuestionPersistencePort;
        this.objectMapper = objectMapper;
        this.microConceptGenerationExecutor = microConceptGenerationExecutor;
    }

    @Override
    public Optional<MicroConceptGenerationTriggerResult> generate(Long programId, Long microConceptId, String requestedBy) {
        if (!isValidId(programId) || !isValidId(microConceptId)) {
            return Optional.empty();
        }
        if (learningProgramPersistencePort.findById(programId).isEmpty()) {
            return Optional.empty();
        }
        boolean belongsToProgram = programMicroConceptReadPort.findByProgramId(programId).stream()
                .anyMatch(target -> microConceptId.equals(target.microConceptId()));
        if (!belongsToProgram) {
            return Optional.empty();
        }

        Optional<MicroConceptGenerationJob> activeJob = microConceptGenerationJobPersistencePort
                .findActiveByProgramIdAndMicroConceptId(programId, microConceptId);
        if (activeJob.isPresent()) {
            return activeJob.map(this::toResult);
        }

        microConceptContentPersistencePort.upsert(
                programId,
                microConceptId,
                MicroConceptContentStatus.GENERATING,
                null,
                null,
                null,
                null,
                null,
                null
        );

        MicroConceptGenerationJob created = microConceptGenerationJobPersistencePort.create(
                programId,
                microConceptId,
                MicroConceptGenerationJobStatus.QUEUED,
                0,
                "Queued for generation",
                normalizeRequestedBy(requestedBy)
        );
        microConceptGenerationExecutor.execute(() -> runGeneration(created.id(), programId, microConceptId));
        return Optional.of(toResult(created));
    }

    private void runGeneration(Long jobId, Long programId, Long microConceptId) {
        try {
            microConceptGenerationJobPersistencePort.updateStatus(
                    jobId,
                    MicroConceptGenerationJobStatus.GENERATING,
                    10,
                    "Generating micro-concept content"
            );

            ProgramMicroConceptTarget target = programMicroConceptReadPort.findByProgramId(programId).stream()
                    .filter(item -> microConceptId.equals(item.microConceptId()))
                    .findFirst()
                    .orElse(null);
            if (target == null) {
                microConceptGenerationJobPersistencePort.updateStatus(
                        jobId,
                        MicroConceptGenerationJobStatus.FAILED,
                        0,
                        "Micro-concept target not found in program"
                );
                microConceptContentPersistencePort.upsert(
                        programId,
                        microConceptId,
                        MicroConceptContentStatus.FAILED,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                return;
            }

            GeneratedQuestionBatch batch = aiQuestionGeneratorPort.generateQuestions(
                    "",
                    nullable(target.topicName()),
                    nullable(target.conceptName()),
                    nullable(target.microConceptName())
            );
            List<GeneratedQuestion> generatedQuestions = batch.questions();
            if (generatedQuestions == null || generatedQuestions.isEmpty()) {
                throw new IllegalStateException("Question generator returned empty result");
            }
            generatedQuestionPersistencePort.save(microConceptId, generatedQuestions);
            GeneratedQuestion primary = generatedQuestions.get(0);
            GeneratedQuestion secondary = generatedQuestions.size() > 1 ? generatedQuestions.get(1) : primary;

            String questionPayload = toJson(Map.of(
                    "text", nullable(primary.text()),
                    "expectedAnswer", nullable(primary.expectedAnswer()),
                    "difficulty", primary.difficulty() == null ? "" : primary.difficulty().name(),
                    "questionType", primary.questionType() == null ? "" : primary.questionType().name()
            ));
            String learningCardPayload = toJson(Map.of(
                    "title", nullable(target.microConceptName()),
                    "explanation", nullable(primary.explanation())
            ));
            List<Map<String, Object>> practiceItems = new ArrayList<>();
            practiceItems.add(buildTrueFalseItem(primary));
            practiceItems.add(buildMultipleChoiceItem(primary, secondary));

            String practicePayload = toJson(practiceItems);
            String quickCheckPayload = toJson(buildQuickCheckItem(primary, secondary));
            String retryPayload = toJson(buildRetryItem(primary, secondary));

            microConceptContentPersistencePort.upsert(
                    programId,
                    microConceptId,
                    MicroConceptContentStatus.READY,
                    questionPayload,
                    learningCardPayload,
                    practicePayload,
                    quickCheckPayload,
                    retryPayload,
                    Instant.now()
            );
            microConceptGenerationJobPersistencePort.updateStatus(
                    jobId,
                    MicroConceptGenerationJobStatus.READY,
                    100,
                    "Generation completed"
            );
        } catch (Exception ex) {
            microConceptGenerationJobPersistencePort.updateStatus(
                    jobId,
                    MicroConceptGenerationJobStatus.FAILED,
                    0,
                    "Generation failed: " + safeMessage(ex)
            );
            microConceptContentPersistencePort.upsert(
                    programId,
                    microConceptId,
                    MicroConceptContentStatus.FAILED,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize payload", ex);
        }
    }

    private String nullable(String value) {
        return value == null ? "" : value;
    }

    private String safeMessage(Exception ex) {
        String message = ex.getMessage();
        return (message == null || message.isBlank()) ? ex.getClass().getSimpleName() : message;
    }

    private MicroConceptGenerationTriggerResult toResult(MicroConceptGenerationJob job) {
        return new MicroConceptGenerationTriggerResult(
                job.id(),
                job.programId(),
                job.microConceptId(),
                job.status(),
                job.progressPercent(),
                job.statusMessage()
        );
    }

    private boolean isValidId(Long value) {
        return value != null && value > 0;
    }

    private String normalizeRequestedBy(String requestedBy) {
        if (requestedBy == null || requestedBy.isBlank()) {
            return null;
        }
        return requestedBy.trim();
    }

    private Map<String, Object> buildTrueFalseItem(GeneratedQuestion primary) {
        Map<String, Object> trueFalse = new LinkedHashMap<>();
        trueFalse.put("type", "TRUE_FALSE");
        trueFalse.put("question", toStatement(primary.expectedAnswer()));
        trueFalse.put("options", List.of());
        trueFalse.put("correctOptions", List.of());
        trueFalse.put("expectedBoolean", true);
        trueFalse.put("correctOrder", List.of());
        trueFalse.put("leftItems", List.of());
        trueFalse.put("rightItems", List.of());
        trueFalse.put("correctMatches", Map.of());
        trueFalse.put("ambiguousIndexing", false);
        return trueFalse;
    }

    private Map<String, Object> buildMultipleChoiceItem(GeneratedQuestion primary, GeneratedQuestion secondary) {
        List<String> options = new ArrayList<>();
        addUnique(options, normalizeOption(primary.expectedAnswer()));
        addUnique(options, normalizeOption(secondary.expectedAnswer()));
        addUnique(options, "A definition of a different concept");
        addUnique(options, "A statement unrelated to this micro concept");
        while (options.size() < 4) {
            options.add("Alternative " + (options.size() + 1));
        }

        Map<String, Object> multipleChoice = new LinkedHashMap<>();
        multipleChoice.put("type", "MULTIPLE_CHOICE");
        multipleChoice.put("question", nonBlank(primary.text(), "Choose the best answer for this micro concept."));
        multipleChoice.put("options", options);
        multipleChoice.put("correctOptions", List.of(0));
        multipleChoice.put("expectedBoolean", null);
        multipleChoice.put("correctOrder", List.of());
        multipleChoice.put("leftItems", List.of());
        multipleChoice.put("rightItems", List.of());
        multipleChoice.put("correctMatches", Map.of());
        multipleChoice.put("ambiguousIndexing", false);
        return multipleChoice;
    }

    private Map<String, Object> buildQuickCheckItem(GeneratedQuestion primary, GeneratedQuestion secondary) {
        return buildMultipleChoiceItem(primary, secondary);
    }

    private Map<String, Object> buildRetryItem(GeneratedQuestion primary, GeneratedQuestion secondary) {
        Map<String, Object> retry = new LinkedHashMap<>(buildMultipleChoiceItem(primary, secondary));
        retry.put("type", "TRUE_FALSE");
        retry.put("question", toStatement(primary.explanation()));
        retry.put("options", List.of());
        retry.put("correctOptions", List.of());
        retry.put("expectedBoolean", true);
        return retry;
    }

    private String toStatement(String text) {
        String value = nonBlank(text, "This statement correctly describes the micro concept.");
        if (value.endsWith(".")) {
            return value;
        }
        return value + ".";
    }

    private String normalizeOption(String value) {
        return nonBlank(value, "A correct explanation of the concept");
    }

    private String nonBlank(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private void addUnique(List<String> target, String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        if (!target.contains(value)) {
            target.add(value);
        }
    }
}
