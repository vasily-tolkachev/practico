package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.GoalProgramLinkJpaEntity;
import com.myproject.practico.application.port.out.GoalProgramLinkPersistencePort;
import com.myproject.practico.domain.GoalProgramLink;
import com.myproject.practico.domain.GoalProgramSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GoalProgramLinkPersistenceAdapter implements GoalProgramLinkPersistencePort {

    private final GoalProgramLinkJpaRepository goalProgramLinkJpaRepository;

    @Override
    public GoalProgramLink create(Long goalId, String programId, GoalProgramSourceType sourceType) {
        GoalProgramLinkJpaEntity created = goalProgramLinkJpaRepository.save(new GoalProgramLinkJpaEntity(
                null,
                goalId,
                programId,
                sourceType,
                Instant.now()
        ));
        return toDomain(created);
    }

    @Override
    public Optional<GoalProgramLink> findByGoalId(Long goalId) {
        return goalProgramLinkJpaRepository.findByGoalId(goalId).map(this::toDomain);
    }

    @Override
    public Optional<GoalProgramLink> findByGoalIdAndProgramId(Long goalId, String programId) {
        return goalProgramLinkJpaRepository.findByGoalIdAndProgramId(goalId, programId).map(this::toDomain);
    }

    private GoalProgramLink toDomain(GoalProgramLinkJpaEntity entity) {
        return new GoalProgramLink(
                entity.getId(),
                entity.getGoalId(),
                entity.getProgramId(),
                entity.getSourceType(),
                entity.getCreatedAt()
        );
    }
}
