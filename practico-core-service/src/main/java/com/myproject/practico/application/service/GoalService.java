package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.CreateGoalUseCase;
import com.myproject.practico.application.port.in.AttachProgramToGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalResolutionStatusUseCase;
import com.myproject.practico.application.port.in.ListGoalsUseCase;
import com.myproject.practico.application.port.in.ProgramResolverUseCase;
import com.myproject.practico.application.port.out.GoalPersistencePort;
import com.myproject.practico.application.port.out.GoalResolutionStatusPort;
import com.myproject.practico.application.program.ProgramResolutionResult;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.GoalResolutionStage;
import com.myproject.practico.domain.GoalResolutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GoalService implements CreateGoalUseCase, ListGoalsUseCase, GetGoalUseCase, GetGoalResolutionStatusUseCase {

    private static final int GENERATION_MAX_ATTEMPTS = 3;
    private static final long GENERATION_RETRY_DELAY_MS = 500L;

    private final GoalPersistencePort goalPersistencePort;
    private final GoalResolutionStatusPort goalResolutionStatusPort;
    private final ProgramResolverUseCase programResolverUseCase;
    private final AttachProgramToGoalUseCase attachProgramToGoalUseCase;

    public GoalService(
            GoalPersistencePort goalPersistencePort,
            GoalResolutionStatusPort goalResolutionStatusPort,
            ProgramResolverUseCase programResolverUseCase,
            AttachProgramToGoalUseCase attachProgramToGoalUseCase
    ) {
        this.goalPersistencePort = goalPersistencePort;
        this.goalResolutionStatusPort = goalResolutionStatusPort;
        this.programResolverUseCase = programResolverUseCase;
        this.attachProgramToGoalUseCase = attachProgramToGoalUseCase;
    }

    @Override
    public Goal create(String title, String description) {
        Goal goal = goalPersistencePort.create(title, description);
        startResolution(goal);
        return goal;
    }

    @Override
    public List<Goal> list() {
        return goalPersistencePort.findAll();
    }

    @Override
    public Optional<Goal> getById(Long goalId) {
        return goalPersistencePort.findById(goalId);
    }

    @Override
    public Optional<GoalResolutionStatus> getByGoalId(Long goalId) {
        return goalResolutionStatusPort.findByGoalId(goalId);
    }

    private void startResolution(Goal goal) {
        if (goal == null || goal.id() == null) {
            return;
        }
        Long goalId = goal.id();

        persistStatus(goalId, GoalResolutionStage.QUEUED, 5, "Goal queued for course resolution");
        CompletableFuture.runAsync(() -> {
            try {
                sleep(350);
                persistStatus(goalId, GoalResolutionStage.GENERATING, 55, "Generating goal-based program");
                ProgramResolutionResult resolved = resolveWithRetry(goalId, goal);
                Long programId = parseProgramId(resolved.program().programId());
                attachProgramToGoalUseCase.attach(goalId, programId, resolved.sourceType());
                persistStatus(goalId, GoalResolutionStage.COMPLETED, 100, "Goal-based program generated");
            } catch (Exception ex) {
                persistStatus(goalId, GoalResolutionStage.FAILED, 100, "Resolution failed");
            }
        });
    }

    private void persistStatus(Long goalId, GoalResolutionStage stage, int progressPercent, String message) {
        goalResolutionStatusPort.save(new GoalResolutionStatus(
                goalId,
                stage,
                progressPercent,
                message,
                Instant.now()
        ));
    }

    private void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
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

    private ProgramResolutionResult resolveWithRetry(Long goalId, Goal goal) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= GENERATION_MAX_ATTEMPTS; attempt++) {
            try {
                if (attempt > 1) {
                    persistStatus(
                            goalId,
                            GoalResolutionStage.GENERATING,
                            55,
                            "Retrying generation (" + attempt + "/" + GENERATION_MAX_ATTEMPTS + ")"
                    );
                }
                return programResolverUseCase.resolveForGoal(goal, "demo-user");
            } catch (RuntimeException ex) {
                last = ex;
                if (attempt < GENERATION_MAX_ATTEMPTS) {
                    try {
                        sleep(GENERATION_RETRY_DELAY_MS);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Generation retry interrupted", interruptedException);
                    }
                }
            }
        }
        throw last == null ? new IllegalStateException("Program generation failed") : last;
    }
}
