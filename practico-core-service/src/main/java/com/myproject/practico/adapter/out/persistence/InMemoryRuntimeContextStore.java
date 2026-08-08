package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.application.port.out.RuntimeContextStore;
import com.myproject.practico.domain.RuntimeContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRuntimeContextStore implements RuntimeContextStore {

    private final Map<String, RuntimeContext> byUserId = new ConcurrentHashMap<>();

    @Override
    public void bind(String userId, Long goalId, String programId) {
        if (userId == null || userId.isBlank() || goalId == null || goalId <= 0 || programId == null || programId.isBlank()) {
            return;
        }
        byUserId.put(userId.trim(), new RuntimeContext(goalId, programId, Instant.now()));
    }

    @Override
    public void bindProgram(String userId, String programId) {
        if (userId == null || userId.isBlank() || programId == null || programId.isBlank()) {
            return;
        }
        byUserId.put(userId.trim(), new RuntimeContext(null, programId, Instant.now()));
    }

    @Override
    public Optional<RuntimeContext> get(String userId) {
        if (userId == null || userId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(byUserId.get(userId.trim()));
    }
}
