package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.LearningProgramJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.MicroConceptContentJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.MicroConceptJpaEntity;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.domain.MicroConceptContent;
import com.myproject.practico.domain.MicroConceptContentStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MicroConceptContentPersistenceAdapter implements MicroConceptContentPersistencePort {

    private final MicroConceptContentJpaRepository microConceptContentJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<MicroConceptContent> findByProgramIdAndMicroConceptId(Long programId, Long microConceptId) {
        return microConceptContentJpaRepository.findByProgram_IdAndMicroConcept_Id(programId, microConceptId)
                .map(this::toDomain);
    }

    @Override
    public MicroConceptContent upsert(
            Long programId,
            Long microConceptId,
            MicroConceptContentStatus status,
            String questionPayload,
            String learningCardPayload,
            String practicePayload,
            String quickCheckPayload,
            String retryPayload,
            Instant generatedAt
    ) {
        Instant now = Instant.now();
        MicroConceptContentJpaEntity saved = microConceptContentJpaRepository
                .findByProgram_IdAndMicroConcept_Id(programId, microConceptId)
                .map(existing -> {
                    existing.setStatus(status);
                    existing.setQuestionPayload(questionPayload);
                    existing.setLearningCardPayload(learningCardPayload);
                    existing.setPracticePayload(practicePayload);
                    existing.setQuickCheckPayload(quickCheckPayload);
                    existing.setRetryPayload(retryPayload);
                    existing.setGeneratedAt(generatedAt);
                    existing.setUpdatedAt(now);
                    return microConceptContentJpaRepository.save(existing);
                })
                .orElseGet(() -> microConceptContentJpaRepository.save(new MicroConceptContentJpaEntity(
                        null,
                        entityManager.getReference(LearningProgramJpaEntity.class, programId),
                        entityManager.getReference(MicroConceptJpaEntity.class, microConceptId),
                        status,
                        questionPayload,
                        learningCardPayload,
                        practicePayload,
                        quickCheckPayload,
                        retryPayload,
                        generatedAt,
                        now
                )));
        return toDomain(saved);
    }

    private MicroConceptContent toDomain(MicroConceptContentJpaEntity entity) {
        return new MicroConceptContent(
                entity.getId(),
                entity.getProgram().getId(),
                entity.getMicroConcept().getId(),
                entity.getStatus(),
                entity.getQuestionPayload(),
                entity.getLearningCardPayload(),
                entity.getPracticePayload(),
                entity.getQuickCheckPayload(),
                entity.getRetryPayload(),
                entity.getGeneratedAt(),
                entity.getUpdatedAt()
        );
    }
}
