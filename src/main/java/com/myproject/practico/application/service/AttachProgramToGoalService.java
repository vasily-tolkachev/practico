package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.AttachProgramToGoalUseCase;
import com.myproject.practico.application.port.out.GoalProgramLinkPersistencePort;
import com.myproject.practico.domain.GoalProgramLink;
import com.myproject.practico.domain.GoalProgramSourceType;

public class AttachProgramToGoalService implements AttachProgramToGoalUseCase {

    private final GoalProgramLinkPersistencePort goalProgramLinkPersistencePort;

    public AttachProgramToGoalService(GoalProgramLinkPersistencePort goalProgramLinkPersistencePort) {
        this.goalProgramLinkPersistencePort = goalProgramLinkPersistencePort;
    }

    @Override
    public GoalProgramLink attach(Long goalId, String programId, GoalProgramSourceType sourceType) {
        if (goalId == null || goalId <= 0) {
            throw new IllegalArgumentException("goalId must be positive");
        }
        if (programId == null || programId.isBlank()) {
            throw new IllegalArgumentException("programId must not be blank");
        }
        GoalProgramSourceType safeSourceType = sourceType == null ? GoalProgramSourceType.GENERATED : sourceType;

        return goalProgramLinkPersistencePort.findByGoalId(goalId)
                .filter(existing -> programId.equals(existing.programId()))
                .or(() -> goalProgramLinkPersistencePort.findByGoalIdAndProgramId(goalId, programId))
                .orElseGet(() -> goalProgramLinkPersistencePort.create(goalId, programId, safeSourceType));
    }
}
