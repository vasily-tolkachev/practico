package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.LearningProgramJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.LearningProgramTopicJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.TopicJpaEntity;
import com.myproject.practico.application.port.out.LearningProgramTopicPersistencePort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class LearningProgramTopicPersistenceAdapter implements LearningProgramTopicPersistencePort {

    private final LearningProgramTopicJpaRepository learningProgramTopicJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void attachTopics(Long programId, List<Long> topicIdsInOrder) {
        if (programId == null || programId <= 0 || topicIdsInOrder == null || topicIdsInOrder.isEmpty()) {
            return;
        }

        List<LearningProgramTopicJpaEntity> existing = learningProgramTopicJpaRepository
                .findByProgram_IdOrderByOrderIndexAsc(programId);
        Set<Long> existingTopicIds = new HashSet<>(existing.stream()
                .map(link -> link.getTopic().getId())
                .toList());
        int nextOrder = existing.size();

        for (Long topicId : topicIdsInOrder) {
            if (topicId == null || topicId <= 0 || existingTopicIds.contains(topicId)) {
                continue;
            }
            learningProgramTopicJpaRepository.save(new LearningProgramTopicJpaEntity(
                    null,
                    entityManager.getReference(LearningProgramJpaEntity.class, programId),
                    entityManager.getReference(TopicJpaEntity.class, topicId),
                    nextOrder++
            ));
            existingTopicIds.add(topicId);
        }
    }

    @Override
    public List<Long> findTopicIds(Long programId) {
        if (programId == null || programId <= 0) {
            return List.of();
        }
        return learningProgramTopicJpaRepository.findByProgram_IdOrderByOrderIndexAsc(programId).stream()
                .map(link -> link.getTopic().getId())
                .toList();
    }
}
