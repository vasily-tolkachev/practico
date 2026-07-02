package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.LearningProgramTopicJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningProgramTopicJpaRepository extends JpaRepository<LearningProgramTopicJpaEntity, Long> {

    List<LearningProgramTopicJpaEntity> findByProgram_IdOrderByOrderIndexAsc(Long programId);

    boolean existsByProgram_IdAndTopic_Id(Long programId, Long topicId);
}
