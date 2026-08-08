package com.myproject.practico.application.service;

import com.myproject.practico.application.microconcept.MicroConceptGeneratedContentResult;
import com.myproject.practico.application.port.in.GetMicroConceptGeneratedContentUseCase;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;

import java.util.Optional;

public class GetMicroConceptGeneratedContentService implements GetMicroConceptGeneratedContentUseCase {

    private final LearningProgramPersistencePort learningProgramPersistencePort;
    private final ProgramMicroConceptReadPort programMicroConceptReadPort;
    private final MicroConceptContentPersistencePort microConceptContentPersistencePort;

    public GetMicroConceptGeneratedContentService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort
    ) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.microConceptContentPersistencePort = microConceptContentPersistencePort;
    }

    @Override
    public Optional<MicroConceptGeneratedContentResult> getContent(Long programId, Long microConceptId) {
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
        return microConceptContentPersistencePort.findByProgramIdAndMicroConceptId(programId, microConceptId)
                .map(content -> new MicroConceptGeneratedContentResult(
                        content.programId(),
                        content.microConceptId(),
                        content.status().name(),
                        content.questionPayload(),
                        content.learningCardPayload(),
                        content.practicePayload(),
                        content.quickCheckPayload(),
                        content.retryPayload(),
                        content.generatedAt(),
                        content.updatedAt()
                ));
    }

    private boolean isValidId(Long value) {
        return value != null && value > 0;
    }
}
