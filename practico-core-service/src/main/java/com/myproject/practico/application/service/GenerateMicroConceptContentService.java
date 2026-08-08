package com.myproject.practico.application.service;

import com.myproject.practico.application.microconcept.MicroConceptGenerationTriggerResult;
import com.myproject.practico.application.port.in.GenerateMicroConceptContentUseCase;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptGenerationJobPersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.domain.MicroConceptContentStatus;
import com.myproject.practico.domain.MicroConceptGenerationJob;
import com.myproject.practico.domain.MicroConceptGenerationJobStatus;

import java.util.Optional;

public class GenerateMicroConceptContentService implements GenerateMicroConceptContentUseCase {

    private final LearningProgramPersistencePort learningProgramPersistencePort;
    private final ProgramMicroConceptReadPort programMicroConceptReadPort;
    private final MicroConceptContentPersistencePort microConceptContentPersistencePort;
    private final MicroConceptGenerationJobPersistencePort microConceptGenerationJobPersistencePort;

    public GenerateMicroConceptContentService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            MicroConceptGenerationJobPersistencePort microConceptGenerationJobPersistencePort
    ) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.microConceptContentPersistencePort = microConceptContentPersistencePort;
        this.microConceptGenerationJobPersistencePort = microConceptGenerationJobPersistencePort;
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
        return Optional.of(toResult(created));
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
