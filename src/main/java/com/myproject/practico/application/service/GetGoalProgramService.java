package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetGoalProgramUseCase;
import com.myproject.practico.application.port.in.ProgramResolverUseCase;
import com.myproject.practico.application.port.out.GoalPersistencePort;
import com.myproject.practico.application.port.out.GoalProgramLinkPersistencePort;
import com.myproject.practico.application.program.LearningProgram;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.GoalProgramLink;

import java.util.Optional;

public class GetGoalProgramService implements GetGoalProgramUseCase {

    private static final String DEFAULT_USER_ID = "demo-user";

    private final GoalPersistencePort goalPersistencePort;
    private final GoalProgramLinkPersistencePort goalProgramLinkPersistencePort;
    private final ProgramResolverUseCase programResolverUseCase;

    public GetGoalProgramService(
            GoalPersistencePort goalPersistencePort,
            GoalProgramLinkPersistencePort goalProgramLinkPersistencePort,
            ProgramResolverUseCase programResolverUseCase
    ) {
        this.goalPersistencePort = goalPersistencePort;
        this.goalProgramLinkPersistencePort = goalProgramLinkPersistencePort;
        this.programResolverUseCase = programResolverUseCase;
    }

    @Override
    public Optional<LearningProgram> getByGoalId(Long goalId, String userId) {
        if (goalId == null || goalId <= 0) {
            return Optional.empty();
        }

        Optional<Goal> goalOptional = goalPersistencePort.findById(goalId);
        Optional<GoalProgramLink> linkOptional = goalProgramLinkPersistencePort.findByGoalId(goalId);
        if (goalOptional.isEmpty() || linkOptional.isEmpty()) {
            return Optional.empty();
        }

        Goal goal = goalOptional.get();
        GoalProgramLink link = linkOptional.get();
        String runtimeUserId = userId == null || userId.isBlank() ? DEFAULT_USER_ID : userId.trim();

        LearningProgram resolved = programResolverUseCase.resolveForGoal(goal, runtimeUserId).program();
        return Optional.of(new LearningProgram(
                link.programId(),
                goal.id(),
                resolved.origin(),
                resolved.title(),
                resolved.goalTitle(),
                resolved.concepts(),
                resolved.progress()
        ));
    }
}
