package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.Goal;

import java.util.List;
import java.util.Optional;

public interface GoalPersistencePort {
    Goal create(String title, String description);

    List<Goal> findAll();

    Optional<Goal> findById(Long goalId);
}
