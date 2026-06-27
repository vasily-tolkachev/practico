package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.UserConceptProgressJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConceptProgressJpaRepository extends JpaRepository<UserConceptProgressJpaEntity, Long> {
}
