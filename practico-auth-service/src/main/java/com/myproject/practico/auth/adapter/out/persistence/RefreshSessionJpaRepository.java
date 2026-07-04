package com.myproject.practico.auth.adapter.out.persistence;

import com.myproject.practico.auth.adapter.out.persistence.entity.RefreshSessionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RefreshSessionJpaRepository extends JpaRepository<RefreshSessionJpaEntity, UUID> {
    Optional<RefreshSessionJpaEntity> findByTokenHash(String tokenHash);
}
