package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.UserConceptProgressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserConceptProgressJpaRepository extends JpaRepository<UserConceptProgressJpaEntity, Long> {
    Optional<UserConceptProgressJpaEntity> findByUser_IdAndConcept_Id(Long userId, Long conceptId);
}
