package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.MicroConceptGenerationJobJpaEntity;
import com.myproject.practico.domain.MicroConceptGenerationJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MicroConceptGenerationJobJpaRepository extends JpaRepository<MicroConceptGenerationJobJpaEntity, Long> {

    Optional<MicroConceptGenerationJobJpaEntity> findFirstByProgram_IdAndMicroConcept_IdOrderByCreatedAtDesc(
            Long programId,
            Long microConceptId
    );

    Optional<MicroConceptGenerationJobJpaEntity> findFirstByProgram_IdAndMicroConcept_IdAndStatusInOrderByCreatedAtDesc(
            Long programId,
            Long microConceptId,
            List<MicroConceptGenerationJobStatus> statuses
    );
}
