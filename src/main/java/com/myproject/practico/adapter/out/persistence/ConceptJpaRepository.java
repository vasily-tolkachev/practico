package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.ConceptJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConceptJpaRepository extends JpaRepository<ConceptJpaEntity, Long> {
}
