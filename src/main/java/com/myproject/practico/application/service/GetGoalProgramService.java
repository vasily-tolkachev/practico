package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetGoalProgramUseCase;
import com.myproject.practico.application.port.out.GoalPersistencePort;
import com.myproject.practico.application.port.out.GoalProgramLinkPersistencePort;
import com.myproject.practico.application.program.LearningProgram;
import com.myproject.practico.application.program.ProgramOrigin;
import com.myproject.practico.application.program.ProgramProgress;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.GoalProgramLink;

import java.util.Optional;

public class GetGoalProgramService implements GetGoalProgramUseCase {

    private final GoalPersistencePort goalPersistencePort;
    private final GoalProgramLinkPersistencePort goalProgramLinkPersistencePort;

    public GetGoalProgramService(
            GoalPersistencePort goalPersistencePort,
            GoalProgramLinkPersistencePort goalProgramLinkPersistencePort
    ) {
        this.goalPersistencePort = goalPersistencePort;
        this.goalProgramLinkPersistencePort = goalProgramLinkPersistencePort;
    }

    @Override
    public Optional<LearningProgram> getByGoalId(Long goalId) {
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
        String goalTitle = goal.title() == null || goal.title().isBlank() ? "Goal" : goal.title().trim();
        return Optional.of(new LearningProgram(
                link.programId(),
                goal.id(),
                ProgramOrigin.GOAL_BASED,
                goalTitle + " Program",
                goalTitle,
                java.util.List.of(),
                new ProgramProgress(0, 0)
        ));
    }
}
