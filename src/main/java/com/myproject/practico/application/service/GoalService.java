package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.CreateGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalResolutionStatusUseCase;
import com.myproject.practico.application.port.in.ListGoalsUseCase;
import com.myproject.practico.application.port.out.GoalPersistencePort;
import com.myproject.practico.application.port.out.GoalResolutionStatusPort;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.GoalResolutionStage;
import com.myproject.practico.domain.GoalResolutionStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class GoalService implements CreateGoalUseCase, ListGoalsUseCase, GetGoalUseCase, GetGoalResolutionStatusUseCase {

    private final GoalPersistencePort goalPersistencePort;
    private final GoalResolutionStatusPort goalResolutionStatusPort;

    public GoalService(
            GoalPersistencePort goalPersistencePort,
            GoalResolutionStatusPort goalResolutionStatusPort
    ) {
        this.goalPersistencePort = goalPersistencePort;
        this.goalResolutionStatusPort = goalResolutionStatusPort;
    }

    @Override
    public Goal create(String title, String description) {
        Goal goal = goalPersistencePort.create(title, description);
        startResolution(goal.id());
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

    private void startResolution(Long goalId) {
        if (goalId == null) {
            return;
        }

        persistStatus(goalId, GoalResolutionStage.QUEUED, 5, "Goal queued for course resolution");
        CompletableFuture.runAsync(() -> {
            try {
                sleep(350);
                persistStatus(goalId, GoalResolutionStage.SEARCHING_LIBRARY, 35, "Searching existing courses");
                sleep(500);
                persistStatus(goalId, GoalResolutionStage.GENERATING, 75, "Generating curriculum");
                sleep(700);
                persistStatus(goalId, GoalResolutionStage.COMPLETED, 100, "Course resolved");
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
}
