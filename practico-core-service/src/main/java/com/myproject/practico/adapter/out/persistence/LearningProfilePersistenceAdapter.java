package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.LearningProfileJpaEntity;
import com.myproject.practico.application.port.out.LearningProfilePersistencePort;
import com.myproject.practico.domain.LearningProfile;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class LearningProfilePersistenceAdapter implements LearningProfilePersistencePort {

    private final LearningProfileJpaRepository learningProfileJpaRepository;

    public LearningProfilePersistenceAdapter(LearningProfileJpaRepository learningProfileJpaRepository) {
        this.learningProfileJpaRepository = learningProfileJpaRepository;
    }

    @Override
    public Optional<LearningProfile> findById(UUID userId) {
        return learningProfileJpaRepository.findById(userId).map(this::toDomain);
    }

    @Override
    public LearningProfile ensureExists(UUID userId, Instant now) {
        LearningProfileJpaEntity entity = learningProfileJpaRepository.findById(userId).orElseGet(() -> learningProfileJpaRepository.save(
                new LearningProfileJpaEntity(userId, "Learner", now, now)
        ));
        return toDomain(entity);
    }

    @Override
    public LearningProfile touch(UUID userId, Instant now) {
        LearningProfileJpaEntity existing = learningProfileJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Learning profile not found: " + userId));
        existing.setUpdatedAt(now);
        return toDomain(learningProfileJpaRepository.save(existing));
    }

    private LearningProfile toDomain(LearningProfileJpaEntity entity) {
        return new LearningProfile(
                entity.getId(),
                entity.getDisplayName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
