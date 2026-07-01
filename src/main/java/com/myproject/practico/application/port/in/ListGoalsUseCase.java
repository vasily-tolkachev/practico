package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.Goal;

import java.util.List;

public interface ListGoalsUseCase {
    List<Goal> list();
}
