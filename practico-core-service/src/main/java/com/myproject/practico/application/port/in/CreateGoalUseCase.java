package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.Goal;

public interface CreateGoalUseCase {
    Goal create(String title, String description);
}
