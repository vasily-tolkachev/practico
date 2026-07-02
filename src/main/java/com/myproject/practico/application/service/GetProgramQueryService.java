package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetProgramByIdUseCase;
import com.myproject.practico.application.port.in.GetProgramStatusUseCase;
import com.myproject.practico.application.port.in.GetProgramTreeUseCase;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.port.out.ProgramTreeReadPort;
import com.myproject.practico.application.program.LearningProgram;
import com.myproject.practico.application.program.ProgramConcept;
import com.myproject.practico.application.program.ProgramGenerationStatus;
import com.myproject.practico.application.program.ProgramOrigin;
import com.myproject.practico.application.program.ProgramProgress;

import java.util.List;
import java.util.Optional;

public class GetProgramQueryService implements GetProgramByIdUseCase, GetProgramTreeUseCase, GetProgramStatusUseCase {

    private final LearningProgramPersistencePort learningProgramPersistencePort;
    private final ProgramTreeReadPort programTreeReadPort;

    public GetProgramQueryService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramTreeReadPort programTreeReadPort
    ) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
        this.programTreeReadPort = programTreeReadPort;
    }

    @Override
    public Optional<com.myproject.practico.domain.LearningProgram> getById(Long programId) {
        if (programId == null || programId <= 0) {
            return Optional.empty();
        }
        return learningProgramPersistencePort.findById(programId);
    }

    @Override
    public Optional<ProgramGenerationStatus> getStatus(Long programId) {
        return getById(programId)
                .map(program -> new ProgramGenerationStatus(
                        program.id(),
                        program.status(),
                        program.updatedAt()
                ));
    }

    @Override
    public Optional<LearningProgram> getTree(Long programId) {
        Optional<com.myproject.practico.domain.LearningProgram> programOptional = getById(programId);
        if (programOptional.isEmpty()) {
            return Optional.empty();
        }

        com.myproject.practico.domain.LearningProgram program = programOptional.get();
        List<ProgramConcept> concepts = programTreeReadPort.findConceptTreeByProgramId(programId);
        int totalMicroConcepts = concepts.stream().mapToInt(concept -> concept.microConcepts().size()).sum();
        return Optional.of(new LearningProgram(
                String.valueOf(program.id()),
                null,
                ProgramOrigin.GOAL_BASED,
                program.title(),
                program.description(),
                concepts,
                new ProgramProgress(concepts.size(), totalMicroConcepts)
        ));
    }
}
