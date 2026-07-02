package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.ProgramResolverUseCase;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.program.LearningProgram;
import com.myproject.practico.application.program.ProgramOrigin;
import com.myproject.practico.application.program.ProgramProgress;
import com.myproject.practico.application.program.ProgramResolutionResult;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.LearningProgramOrigin;
import com.myproject.practico.domain.LearningProgramStatus;
import com.myproject.practico.domain.GoalProgramSourceType;

import java.util.List;

public class ProgramResolverService implements ProgramResolverUseCase {

    private final LearningProgramPersistencePort learningProgramPersistencePort;

    public ProgramResolverService(LearningProgramPersistencePort learningProgramPersistencePort) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
    }

    @Override
    public ProgramResolutionResult resolveForGoal(Goal goal, String userId) {
        String goalTitle = goal == null || goal.title() == null || goal.title().isBlank()
                ? "Goal"
                : goal.title().trim();
        com.myproject.practico.domain.LearningProgram persistedProgram = learningProgramPersistencePort.create(
                goalTitle + " Program",
                "Generated from goal: " + goalTitle,
                LearningProgramStatus.CREATED,
                LearningProgramOrigin.GOAL_BASED
        );

        return new ProgramResolutionResult(
                new LearningProgram(
                        String.valueOf(persistedProgram.id()),
                        goal == null ? null : goal.id(),
                        ProgramOrigin.GOAL_BASED,
                        persistedProgram.title(),
                        goalTitle,
                        List.of(),
                        new ProgramProgress(0, 0)
                ),
                GoalProgramSourceType.GENERATED
        );
    }
}
