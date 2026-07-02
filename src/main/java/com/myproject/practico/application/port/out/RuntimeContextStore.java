package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.RuntimeContext;

import java.util.Optional;

public interface RuntimeContextStore {
    void bind(String userId, Long goalId, String programId);

    Optional<RuntimeContext> get(String userId);
}
