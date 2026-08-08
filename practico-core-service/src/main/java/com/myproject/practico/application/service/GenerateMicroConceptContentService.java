package com.myproject.practico.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myproject.practico.application.microconcept.MicroConceptGenerationTriggerResult;
import com.myproject.practico.application.port.in.GenerateMicroConceptContentUseCase;
import com.myproject.practico.application.port.out.AiQuestionGeneratorPort;
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
    private final ObjectMapper objectMapper;
    private final Executor microConceptGenerationExecutor;

    public GenerateMicroConceptContentService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            MicroConceptGenerationJobPersistencePort microConceptGenerationJobPersistencePort,
            AiQuestionGeneratorPort aiQuestionGeneratorPort,
            ObjectMapper objectMapper,
            Executor microConceptGenerationExecutor
    ) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.microConceptContentPersistencePort = microConceptContentPersistencePort;
        this.microConceptGenerationJobPersistencePort = microConceptGenerationJobPersistencePort;
        this.aiQuestionGeneratorPort = aiQuestionGeneratorPort;
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
            String quickCheckPayload = toJson(Map.of(
                    "question", nullable(secondary.text()),
                    "expectedAnswer", nullable(secondary.expectedAnswer())
            ));
            String practicePayload = toJson(List.of(
                    Map.of(
                            "type", "TRUE_FALSE",
                            "question", "I understand: " + nullable(primary.text()),
                            "options", List.of(),
                            "correctOptions", List.of(),
                            "expectedBoolean", true,
                            "ambiguousIndexing", false
                    )
            ));
            String retryPayload = toJson(Map.of(
                    "rubric", List.of("Give the key idea", "Use one concrete example"),
                    "question", nullable(primary.text())
            ));

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
}
