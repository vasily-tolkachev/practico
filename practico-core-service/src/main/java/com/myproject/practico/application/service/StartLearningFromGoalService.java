package com.myproject.practico.application.service;

import com.myproject.practico.application.goal.GoalLearningStartResult;
import com.myproject.practico.application.port.in.AttachProgramToGoalUseCase;
import com.myproject.practico.application.port.in.ProgramResolverUseCase;
import com.myproject.practico.application.port.in.StartLearningFromGoalUseCase;
import com.myproject.practico.application.port.in.StartLearningUseCase;
import com.myproject.practico.application.port.out.GoalPersistencePort;
import com.myproject.practico.application.port.out.GoalProgramLinkPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.application.port.out.RuntimeContextStore;
import com.myproject.practico.domain.MicroConceptContentStatus;
import com.myproject.practico.application.program.ProgramResolutionResult;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.GoalProgramLink;

import java.util.Optional;

public class StartLearningFromGoalService implements StartLearningFromGoalUseCase {

    private static final String DEFAULT_USER_ID = "demo-user";

    private final GoalPersistencePort goalPersistencePort;
    private final GoalProgramLinkPersistencePort goalProgramLinkPersistencePort;
    private final ProgramResolverUseCase programResolverUseCase;
    private final AttachProgramToGoalUseCase attachProgramToGoalUseCase;
    private final StartLearningUseCase startLearningUseCase;
    private final ProgramMicroConceptReadPort programMicroConceptReadPort;
    private final MicroConceptContentPersistencePort microConceptContentPersistencePort;
    private final RuntimeContextStore runtimeContextStore;

    public StartLearningFromGoalService(
            GoalPersistencePort goalPersistencePort,
            GoalProgramLinkPersistencePort goalProgramLinkPersistencePort,
            ProgramResolverUseCase programResolverUseCase,
            AttachProgramToGoalUseCase attachProgramToGoalUseCase,
            StartLearningUseCase startLearningUseCase,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            RuntimeContextStore runtimeContextStore
    ) {
        this.goalPersistencePort = goalPersistencePort;
        this.goalProgramLinkPersistencePort = goalProgramLinkPersistencePort;
        this.programResolverUseCase = programResolverUseCase;
        this.attachProgramToGoalUseCase = attachProgramToGoalUseCase;
        this.startLearningUseCase = startLearningUseCase;
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.microConceptContentPersistencePort = microConceptContentPersistencePort;
        this.runtimeContextStore = runtimeContextStore;
    }

    @Override
    public Optional<GoalLearningStartResult> start(Long goalId, String userId) {
        if (goalId == null || goalId <= 0) {
            return Optional.empty();
        }

        Optional<Goal> goalOptional = goalPersistencePort.findById(goalId);
        if (goalOptional.isEmpty()) {
            return Optional.empty();
        }
        Goal goal = goalOptional.get();
        String runtimeUserId = isBlank(userId) ? DEFAULT_USER_ID : userId.trim();

        GoalProgramLink goalProgramLink = goalProgramLinkPersistencePort.findByGoalId(goalId)
                .orElseGet(() -> {
                    ProgramResolutionResult resolved = programResolverUseCase.resolveForGoal(goal, runtimeUserId);
                    Long programId = parseProgramId(resolved.program().programId());
                    return attachProgramToGoalUseCase.attach(goalId, programId, resolved.sourceType());
                });
        if (!isFirstMicroConceptReady(goalProgramLink.programId())) {
            return Optional.of(new GoalLearningStartResult(
                    goalId,
                    String.valueOf(goalProgramLink.programId()),
                    "MICRO_CONCEPT_CONTENT_NOT_READY"
            ));
        }

        runtimeContextStore.bind(runtimeUserId, goalId, String.valueOf(goalProgramLink.programId()));
        startLearningUseCase.start(runtimeUserId);
        return Optional.of(new GoalLearningStartResult(goalId, String.valueOf(goalProgramLink.programId()), "READY"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Long parseProgramId(String programId) {
        if (programId == null || programId.isBlank()) {
            throw new IllegalStateException("Program id is blank");
        }
        try {
            return Long.parseLong(programId);
        } catch (NumberFormatException ex) {
            throw new IllegalStateException("Program id must be numeric: " + programId, ex);
        }
    }

    private boolean isFirstMicroConceptReady(Long programId) {
        if (programId == null || programId <= 0) {
            return false;
        }
        Long firstMicroConceptId = programMicroConceptReadPort.findByProgramId(programId).stream()
                .map(target -> target.microConceptId())
                .findFirst()
                .orElse(null);
        if (firstMicroConceptId == null || firstMicroConceptId <= 0) {
            return false;
        }
        return microConceptContentPersistencePort.findByProgramIdAndMicroConceptId(programId, firstMicroConceptId)
                .map(content -> content.status() == MicroConceptContentStatus.READY)
                .orElse(false);
    }
}
