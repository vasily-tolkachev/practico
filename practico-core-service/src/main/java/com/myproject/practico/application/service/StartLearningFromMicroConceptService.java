package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.StartLearningFromMicroConceptUseCase;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.application.port.out.RuntimeContextStore;
import com.myproject.practico.domain.MicroConceptContentStatus;
import com.myproject.practico.domain.Question;

import java.util.Optional;

public class StartLearningFromMicroConceptService implements StartLearningFromMicroConceptUseCase {

    private final LearningProgramPersistencePort learningProgramPersistencePort;
    private final ProgramMicroConceptReadPort programMicroConceptReadPort;
    private final MicroConceptContentPersistencePort microConceptContentPersistencePort;
    private final QuestionPersistencePort questionPersistencePort;
    private final LearningSessionService learningSessionService;
    private final LearningStateAssembler learningStateAssembler;
    private final RuntimeContextStore runtimeContextStore;

    public StartLearningFromMicroConceptService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            QuestionPersistencePort questionPersistencePort,
            LearningSessionService learningSessionService,
            LearningStateAssembler learningStateAssembler,
            RuntimeContextStore runtimeContextStore
    ) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.microConceptContentPersistencePort = microConceptContentPersistencePort;
        this.questionPersistencePort = questionPersistencePort;
        this.learningSessionService = learningSessionService;
        this.learningStateAssembler = learningStateAssembler;
        this.runtimeContextStore = runtimeContextStore;
    }

    @Override
    public Optional<LearningState> start(Long programId, Long microConceptId, String userId) {
        if (!isValidId(programId) || !isValidId(microConceptId) || isBlank(userId)) {
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
        boolean ready = microConceptContentPersistencePort
                .findByProgramIdAndMicroConceptId(programId, microConceptId)
                .map(content -> content.status() == MicroConceptContentStatus.READY)
                .orElse(false);
        if (!ready) {
            return Optional.empty();
        }

        Question firstQuestion = questionPersistencePort.findAll().stream()
                .filter(question -> question.microConcept() != null && microConceptId.equals(question.microConcept().id()))
                .findFirst()
                .orElse(null);
        if (firstQuestion == null || firstQuestion.id() == null || firstQuestion.concept() == null || firstQuestion.concept().id() == null) {
            return Optional.empty();
        }

        runtimeContextStore.bindProgram(userId.trim(), String.valueOf(programId));
        learningSessionService.startLearningSession(userId.trim(), firstQuestion.concept().id(), firstQuestion.id());
        return Optional.of(learningStateAssembler.assemble(userId.trim(), learningSessionService.getSession(userId.trim()).orElse(null)));
    }

    private boolean isValidId(Long value) {
        return value != null && value > 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
