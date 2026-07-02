package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.GoalProgramLinkJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GoalProgramLinkJpaRepository extends JpaRepository<GoalProgramLinkJpaEntity, Long> {
    Optional<GoalProgramLinkJpaEntity> findByGoalId(Long goalId);

    Optional<GoalProgramLinkJpaEntity> findByGoalIdAndProgramId(Long goalId, String programId);
}
