package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.LearningProgramJpaEntity;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.domain.LearningProgram;
import com.myproject.practico.domain.LearningProgramOrigin;
import com.myproject.practico.domain.LearningProgramStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LearningProgramPersistenceAdapter implements LearningProgramPersistencePort {

    private final LearningProgramJpaRepository learningProgramJpaRepository;

    @Override
    public LearningProgram create(
            String title,
            String description,
            LearningProgramStatus status,
            LearningProgramOrigin origin
    ) {
        Instant now = Instant.now();
        LearningProgramJpaEntity created = learningProgramJpaRepository.save(new LearningProgramJpaEntity(
                null,
                title,
                description,
                status,
                origin,
                now,
                now
        ));
        return toDomain(created);
    }

    @Override
    public Optional<LearningProgram> findById(Long id) {
        return learningProgramJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<LearningProgram> updateStatus(Long id, LearningProgramStatus status) {
        return learningProgramJpaRepository.findById(id)
                .map(existing -> {
                    existing.setStatus(status);
                    existing.setUpdatedAt(Instant.now());
                    return learningProgramJpaRepository.save(existing);
                })
                .map(this::toDomain);
    }

    private LearningProgram toDomain(LearningProgramJpaEntity entity) {
        return new LearningProgram(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getOrigin(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
