package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.application.port.out.GoalResolutionStatusPort;
import com.myproject.practico.domain.GoalResolutionStatus;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGoalResolutionStatusAdapter implements GoalResolutionStatusPort {

    private final Map<Long, GoalResolutionStatus> storage = new ConcurrentHashMap<>();

    @Override
    public void save(GoalResolutionStatus status) {
        if (status == null || status.goalId() == null) {
            return;
        }
        storage.put(status.goalId(), status);
    }

    @Override
    public Optional<GoalResolutionStatus> findByGoalId(Long goalId) {
        return Optional.ofNullable(storage.get(goalId));
    }
}
