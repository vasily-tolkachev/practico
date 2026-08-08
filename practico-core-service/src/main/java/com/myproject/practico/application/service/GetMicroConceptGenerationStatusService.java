package com.myproject.practico.application.service;

import com.myproject.practico.application.microconcept.MicroConceptGenerationStatusResult;
import com.myproject.practico.application.port.in.GetMicroConceptGenerationStatusUseCase;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptGenerationJobPersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.domain.MicroConceptGenerationJob;
import com.myproject.practico.domain.MicroConceptGenerationJobStatus;

import java.util.Optional;

public class GetMicroConceptGenerationStatusService implements GetMicroConceptGenerationStatusUseCase {

    private final LearningProgramPersistencePort learningProgramPersistencePort;
    private final ProgramMicroConceptReadPort programMicroConceptReadPort;
    private final MicroConceptGenerationJobPersistencePort microConceptGenerationJobPersistencePort;
    private final MicroConceptContentPersistencePort microConceptContentPersistencePort;

    public GetMicroConceptGenerationStatusService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptGenerationJobPersistencePort microConceptGenerationJobPersistencePort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort
    ) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.microConceptGenerationJobPersistencePort = microConceptGenerationJobPersistencePort;
        this.microConceptContentPersistencePort = microConceptContentPersistencePort;
    }

    @Override
    public Optional<MicroConceptGenerationStatusResult> getStatus(Long programId, Long microConceptId) {
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

        Optional<MicroConceptGenerationJob> latestJob = microConceptGenerationJobPersistencePort
                .findLatestByProgramIdAndMicroConceptId(programId, microConceptId);
        if (latestJob.isPresent()) {
            return latestJob.map(this::fromJob);
        }

        boolean hasContent = microConceptContentPersistencePort
                .findByProgramIdAndMicroConceptId(programId, microConceptId)
                .isPresent();
        if (hasContent) {
            return Optional.of(new MicroConceptGenerationStatusResult(
                    programId,
                    microConceptId,
                    null,
                    "READY",
                    100,
                    "Content is available",
                    null
            ));
        }

        return Optional.of(new MicroConceptGenerationStatusResult(
                programId,
                microConceptId,
                null,
                "NOT_STARTED",
                0,
                "Generation not started",
                null
        ));
    }

    private MicroConceptGenerationStatusResult fromJob(MicroConceptGenerationJob job) {
        String status = mapStatus(job.status());
        int progress = job.progressPercent() == null ? defaultProgress(status) : job.progressPercent();
        return new MicroConceptGenerationStatusResult(
                job.programId(),
                job.microConceptId(),
                job.id(),
                status,
                progress,
                job.statusMessage(),
                job.updatedAt()
        );
    }

    private String mapStatus(MicroConceptGenerationJobStatus status) {
        if (status == MicroConceptGenerationJobStatus.READY) return "READY";
        if (status == MicroConceptGenerationJobStatus.FAILED) return "FAILED";
        return "GENERATING";
    }

    private int defaultProgress(String status) {
        if ("READY".equals(status)) return 100;
        if ("FAILED".equals(status)) return 0;
        return 0;
    }

    private boolean isValidId(Long value) {
        return value != null && value > 0;
    }
}
