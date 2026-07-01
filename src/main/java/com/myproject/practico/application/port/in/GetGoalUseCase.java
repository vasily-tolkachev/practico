package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.Goal;

import java.util.Optional;

public interface GetGoalUseCase {
    Optional<Goal> getById(Long goalId);
}
