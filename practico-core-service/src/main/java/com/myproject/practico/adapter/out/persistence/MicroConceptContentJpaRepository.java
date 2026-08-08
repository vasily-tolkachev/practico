package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.MicroConceptContentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MicroConceptContentJpaRepository extends JpaRepository<MicroConceptContentJpaEntity, Long> {

    Optional<MicroConceptContentJpaEntity> findByProgram_IdAndMicroConcept_Id(Long programId, Long microConceptId);
}
