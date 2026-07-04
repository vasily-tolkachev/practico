package com.myproject.practico.application.service;

import com.myproject.practico.application.port.out.AiQuestionGeneratorPort;
import com.myproject.practico.application.port.out.GenerationMetricsPort;
import com.myproject.practico.application.port.out.GeneratedQuestionPersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.application.program.GeneratedQuestionBatch;
import com.myproject.practico.application.program.GeneratedQuestion;
import com.myproject.practico.application.program.GenerationStage;
import com.myproject.practico.application.program.ProgramMicroConceptTarget;

import java.util.List;

public class ProgramQuestionGenerationService {

    private static final int MAX_ATTEMPTS_PER_MICRO_CONCEPT = 3;
    private static final long RETRY_DELAY_MS = 300L;

    private final ProgramMicroConceptReadPort programMicroConceptReadPort;
    private final AiQuestionGeneratorPort aiQuestionGeneratorPort;
    private final GeneratedQuestionPersistencePort generatedQuestionPersistencePort;
    private final GenerationMetricsPort generationMetricsPort;

    public ProgramQuestionGenerationService(
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            AiQuestionGeneratorPort aiQuestionGeneratorPort,
            GeneratedQuestionPersistencePort generatedQuestionPersistencePort,
            GenerationMetricsPort generationMetricsPort
    ) {
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.aiQuestionGeneratorPort = aiQuestionGeneratorPort;
        this.generatedQuestionPersistencePort = generatedQuestionPersistencePort;
        this.generationMetricsPort = generationMetricsPort;
    }

    public void generateForProgram(Long programId, String goalTitle) {
        List<ProgramMicroConceptTarget> targets = programMicroConceptReadPort.findByProgramId(programId);
        for (ProgramMicroConceptTarget target : targets) {
            if (target.microConceptId() == null || programMicroConceptReadPort.hasQuestions(target.microConceptId())) {
                continue;
            }
            generateWithRetry(goalTitle, target);
        }
    }

    private void generateWithRetry(String goalTitle, ProgramMicroConceptTarget target) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS_PER_MICRO_CONCEPT; attempt++) {
            long startedAt = System.currentTimeMillis();
            try {
                GeneratedQuestionBatch batch = aiQuestionGeneratorPort.generateQuestions(
                        goalTitle,
                        target.topicName(),
                        target.conceptName(),
                        target.microConceptName()
                );
                List<GeneratedQuestion> generated = batch.questions();
                if (generated != null && !generated.isEmpty()) {
                    generatedQuestionPersistencePort.save(target.microConceptId(), generated);
                }
                generationMetricsPort.recordSuccess(
                        GenerationStage.QUESTION_GENERATION,
                        System.currentTimeMillis() - startedAt,
                        batch.totalTokens(),
                        batch.estimatedCostUsd()
                );
                return;
            } catch (Exception ex) {
                generationMetricsPort.recordFailure(
                        GenerationStage.QUESTION_GENERATION,
                        System.currentTimeMillis() - startedAt,
                        null,
                        null
                );
                if (attempt >= MAX_ATTEMPTS_PER_MICRO_CONCEPT) {
                    return;
                }
                sleepRetryDelay();
            }
        }
    }

    private void sleepRetryDelay() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
