package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.GoalJpaEntity;
import com.myproject.practico.application.port.out.GoalPersistencePort;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.GoalStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GoalPersistenceAdapter implements GoalPersistencePort {

    private final GoalJpaRepository goalJpaRepository;

    @Override
    public Goal create(String title, String description) {
        GoalJpaEntity created = goalJpaRepository.save(new GoalJpaEntity(
                null,
                title,
                description,
                GoalStatus.ACTIVE,
                Instant.now()
        ));
        return toDomain(created);
    }

    @Override
    public List<Goal> findAll() {
        return goalJpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Goal> findById(Long goalId) {
        return goalJpaRepository.findById(goalId).map(this::toDomain);
    }

    private Goal toDomain(GoalJpaEntity entity) {
        return new Goal(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
