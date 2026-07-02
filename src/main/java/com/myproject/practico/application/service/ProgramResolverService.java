package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.ProgramResolverUseCase;
import com.myproject.practico.application.program.LearningProgram;
import com.myproject.practico.application.program.ProgramOrigin;
import com.myproject.practico.application.program.ProgramProgress;
import com.myproject.practico.application.program.ProgramResolutionResult;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.GoalProgramSourceType;

import java.util.List;

public class ProgramResolverService implements ProgramResolverUseCase {

    @Override
    public ProgramResolutionResult resolveForGoal(Goal goal, String userId) {
        String goalTitle = goal == null || goal.title() == null || goal.title().isBlank()
                ? "Goal"
                : goal.title().trim();
        String placeholderProgramId = "goal-" + (goal == null || goal.id() == null ? "new" : goal.id());

        return new ProgramResolutionResult(
                new LearningProgram(
                        placeholderProgramId,
                        goal == null ? null : goal.id(),
                        ProgramOrigin.GOAL_BASED,
                        goalTitle + " Program",
                        goalTitle,
                        List.of(),
                        new ProgramProgress(0, 0)
                ),
                GoalProgramSourceType.GENERATED
        );
    }
}
