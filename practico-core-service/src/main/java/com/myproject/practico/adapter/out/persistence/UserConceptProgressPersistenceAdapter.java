package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.ConceptJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.ProgressStatusJpa;
import com.myproject.practico.adapter.out.persistence.entity.UserConceptProgressJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.UserJpaEntity;
import com.myproject.practico.application.port.out.UserConceptProgressPersistencePort;
import com.myproject.practico.domain.ProgressStatus;
import com.myproject.practico.domain.UserConceptProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserConceptProgressPersistenceAdapter implements UserConceptProgressPersistencePort {

    private final UserConceptProgressJpaRepository userConceptProgressJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ConceptJpaRepository conceptJpaRepository;

    @Override
    public Optional<UserConceptProgress> findByUserIdAndConceptId(Long userId, Long conceptId) {
        return userConceptProgressJpaRepository.findByUser_IdAndConcept_Id(userId, conceptId)
                .map(this::toDomain);
    }

    @Override
    public UserConceptProgress upsert(
            Long userId,
            Long conceptId,
            ProgressStatus status,
            int correctAnswers,
            int totalAnswers,
            Instant updatedAt
    ) {
        UserConceptProgressJpaEntity existing = userConceptProgressJpaRepository
                .findByUser_IdAndConcept_Id(userId, conceptId)
                .orElse(null);

        UserJpaEntity user = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found for concept progress persistence"));
        ConceptJpaEntity concept = conceptJpaRepository.findById(conceptId)
                .orElseThrow(() -> new IllegalStateException("Concept not found for concept progress persistence"));

        if (existing == null) {
            UserConceptProgressJpaEntity created = userConceptProgressJpaRepository.save(new UserConceptProgressJpaEntity(
                    null,
                    user,
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
                entity.getUser().getId(),
                entity.getConcept().getId(),
                ProgressStatus.valueOf(entity.getStatus().name()),
                entity.getCorrectAnswers(),
                entity.getTotalAnswers(),
                entity.getUpdatedAt()
        );
    }
}
