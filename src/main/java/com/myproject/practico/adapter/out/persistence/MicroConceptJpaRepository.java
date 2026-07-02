package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.MicroConceptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MicroConceptJpaRepository extends JpaRepository<MicroConceptJpaEntity, Long> {
}
