package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.ConceptJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.LearningProfileJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.ProgressStatusJpa;
import com.myproject.practico.adapter.out.persistence.entity.UserConceptProgressJpaEntity;
import com.myproject.practico.application.port.out.UserConceptProgressPersistencePort;
import com.myproject.practico.domain.ProgressStatus;
import com.myproject.practico.domain.UserConceptProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserConceptProgressPersistenceAdapter implements UserConceptProgressPersistencePort {

    private final UserConceptProgressJpaRepository userConceptProgressJpaRepository;
    private final LearningProfileJpaRepository learningProfileJpaRepository;
    private final ConceptJpaRepository conceptJpaRepository;

    @Override
    public Optional<UserConceptProgress> findByProfileIdAndConceptId(UUID profileId, Long conceptId) {
        return userConceptProgressJpaRepository.findByProfile_IdAndConcept_Id(profileId, conceptId)
                .map(this::toDomain);
    }

    @Override
    public UserConceptProgress upsert(
            UUID profileId,
            Long conceptId,
            ProgressStatus status,
            int correctAnswers,
            int totalAnswers,
            Instant updatedAt
    ) {
        UserConceptProgressJpaEntity existing = userConceptProgressJpaRepository
                .findByProfile_IdAndConcept_Id(profileId, conceptId)
                .orElse(null);

        LearningProfileJpaEntity profile = learningProfileJpaRepository.findById(profileId)
                .orElseThrow(() -> new IllegalStateException("Learning profile not found for concept progress persistence"));
        ConceptJpaEntity concept = conceptJpaRepository.findById(conceptId)
                .orElseThrow(() -> new IllegalStateException("Concept not found for concept progress persistence"));

        if (existing == null) {
            UserConceptProgressJpaEntity created = userConceptProgressJpaRepository.save(new UserConceptProgressJpaEntity(
                    null,
                    profile,
                    concept,
                    ProgressStatusJpa.valueOf(status.name()),
                    correctAnswers,
                    totalAnswers,
                    updatedAt
            ));
            return toDomain(created);
        }

        existing.setStatus(ProgressStatusJpa.valueOf(status.name()));
        existing.setCorrectAnswers(correctAnswers);
        existing.setTotalAnswers(totalAnswers);
        existing.setUpdatedAt(updatedAt);

        UserConceptProgressJpaEntity updated = userConceptProgressJpaRepository.save(existing);
        return toDomain(updated);
    }

    private UserConceptProgress toDomain(UserConceptProgressJpaEntity entity) {
        return new UserConceptProgress(
                entity.getId(),
                entity.getProfile().getId(),
                entity.getConcept().getId(),
                ProgressStatus.valueOf(entity.getStatus().name()),
                entity.getCorrectAnswers(),
                entity.getTotalAnswers(),
                entity.getUpdatedAt()
        );
    }
}
