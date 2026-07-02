package com.myproject.practico.application.service;

import com.myproject.practico.application.goal.GoalLearningStartResult;
import com.myproject.practico.application.port.in.AttachProgramToGoalUseCase;
import com.myproject.practico.application.port.in.ProgramResolverUseCase;
import com.myproject.practico.application.port.in.StartLearningFromGoalUseCase;
import com.myproject.practico.application.port.in.StartLearningUseCase;
import com.myproject.practico.application.port.out.GoalPersistencePort;
import com.myproject.practico.application.port.out.GoalProgramLinkPersistencePort;
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

    public StartLearningFromGoalService(
            GoalPersistencePort goalPersistencePort,
            GoalProgramLinkPersistencePort goalProgramLinkPersistencePort,
            ProgramResolverUseCase programResolverUseCase,
            AttachProgramToGoalUseCase attachProgramToGoalUseCase,
            StartLearningUseCase startLearningUseCase
    ) {
        this.goalPersistencePort = goalPersistencePort;
        this.goalProgramLinkPersistencePort = goalProgramLinkPersistencePort;
        this.programResolverUseCase = programResolverUseCase;
        this.attachProgramToGoalUseCase = attachProgramToGoalUseCase;
        this.startLearningUseCase = startLearningUseCase;
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
                    return attachProgramToGoalUseCase.attach(goalId, resolved.program().programId(), resolved.sourceType());
                });

        startLearningUseCase.start(runtimeUserId);
        return Optional.of(new GoalLearningStartResult(goalId, goalProgramLink.programId(), "READY"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
