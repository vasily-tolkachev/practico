package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.CreateGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalUseCase;
import com.myproject.practico.application.port.in.ListGoalsUseCase;
import com.myproject.practico.application.port.out.GoalPersistencePort;
import com.myproject.practico.domain.Goal;

import java.util.List;
import java.util.Optional;

public class GoalService implements CreateGoalUseCase, ListGoalsUseCase, GetGoalUseCase {

    private final GoalPersistencePort goalPersistencePort;

    public GoalService(GoalPersistencePort goalPersistencePort) {
        this.goalPersistencePort = goalPersistencePort;
    }

    @Override
    public Goal create(String title, String description) {
        return goalPersistencePort.create(title, description);
    }

    @Override
    public List<Goal> list() {
        return goalPersistencePort.findAll();
    }

    @Override
    public Optional<Goal> getById(Long goalId) {
        return goalPersistencePort.findById(goalId);
    }
}
