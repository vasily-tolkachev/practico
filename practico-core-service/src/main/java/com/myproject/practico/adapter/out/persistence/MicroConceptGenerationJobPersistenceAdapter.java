package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.LearningProgramJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.MicroConceptGenerationJobJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.MicroConceptJpaEntity;
import com.myproject.practico.application.port.out.MicroConceptGenerationJobPersistencePort;
import com.myproject.practico.domain.MicroConceptGenerationJob;
import com.myproject.practico.domain.MicroConceptGenerationJobStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MicroConceptGenerationJobPersistenceAdapter implements MicroConceptGenerationJobPersistencePort {

    private static final List<MicroConceptGenerationJobStatus> ACTIVE_STATUSES = List.of(
            MicroConceptGenerationJobStatus.QUEUED,
            MicroConceptGenerationJobStatus.GENERATING
    );

    private final MicroConceptGenerationJobJpaRepository microConceptGenerationJobJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<MicroConceptGenerationJob> findActiveByProgramIdAndMicroConceptId(Long programId, Long microConceptId) {
        return microConceptGenerationJobJpaRepository
                .findFirstByProgram_IdAndMicroConcept_IdAndStatusInOrderByCreatedAtDesc(programId, microConceptId, ACTIVE_STATUSES)
                .map(this::toDomain);
    }

    @Override
    public Optional<MicroConceptGenerationJob> findLatestByProgramIdAndMicroConceptId(Long programId, Long microConceptId) {
        return microConceptGenerationJobJpaRepository
                .findFirstByProgram_IdAndMicroConcept_IdOrderByCreatedAtDesc(programId, microConceptId)
                .map(this::toDomain);
    }

    @Override
    public MicroConceptGenerationJob create(
            Long programId,
            Long microConceptId,
            MicroConceptGenerationJobStatus status,
            Integer progressPercent,
            String statusMessage,
            String requestedBy
    ) {
        Instant now = Instant.now();
        MicroConceptGenerationJobJpaEntity created = microConceptGenerationJobJpaRepository.save(new MicroConceptGenerationJobJpaEntity(
                null,
                entityManager.getReference(LearningProgramJpaEntity.class, programId),
                entityManager.getReference(MicroConceptJpaEntity.class, microConceptId),
                status,
                progressPercent,
                statusMessage,
                requestedBy,
                now,
                now
        ));
        return toDomain(created);
    }

    @Override
    public Optional<MicroConceptGenerationJob> updateStatus(
            Long jobId,
            MicroConceptGenerationJobStatus status,
            Integer progressPercent,
            String statusMessage
    ) {
        return microConceptGenerationJobJpaRepository.findById(jobId)
                .map(existing -> {
                    existing.setStatus(status);
                    existing.setProgressPercent(progressPercent);
                    existing.setStatusMessage(statusMessage);
                    existing.setUpdatedAt(Instant.now());
                    return microConceptGenerationJobJpaRepository.save(existing);
                })
                .map(this::toDomain);
    }

    private MicroConceptGenerationJob toDomain(MicroConceptGenerationJobJpaEntity entity) {
        return new MicroConceptGenerationJob(
                entity.getId(),
                entity.getProgram().getId(),
                entity.getMicroConcept().getId(),
                entity.getStatus(),
                entity.getProgressPercent(),
                entity.getStatusMessage(),
                entity.getRequestedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
