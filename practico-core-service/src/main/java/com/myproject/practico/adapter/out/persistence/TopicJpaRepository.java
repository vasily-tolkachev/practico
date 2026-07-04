package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.TopicJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopicJpaRepository extends JpaRepository<TopicJpaEntity, Long> {
    Optional<TopicJpaEntity> findByName(String name);
}
