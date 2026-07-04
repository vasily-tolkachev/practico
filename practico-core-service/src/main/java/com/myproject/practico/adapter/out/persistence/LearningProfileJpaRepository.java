package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.LearningProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LearningProfileJpaRepository extends JpaRepository<LearningProfileJpaEntity, UUID> {
}
