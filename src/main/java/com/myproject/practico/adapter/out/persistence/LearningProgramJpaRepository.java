package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.LearningProgramJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningProgramJpaRepository extends JpaRepository<LearningProgramJpaEntity, Long> {
}
