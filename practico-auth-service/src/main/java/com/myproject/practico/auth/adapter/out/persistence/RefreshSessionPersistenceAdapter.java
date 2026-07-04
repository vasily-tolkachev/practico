package com.myproject.practico.auth.adapter.out.persistence;

import com.myproject.practico.auth.adapter.out.persistence.entity.RefreshSessionJpaEntity;
import com.myproject.practico.auth.adapter.out.persistence.entity.UserJpaEntity;
import com.myproject.practico.auth.application.port.RefreshSessionRepository;
import com.myproject.practico.auth.domain.RefreshSession;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshSessionPersistenceAdapter implements RefreshSessionRepository {

    private final RefreshSessionJpaRepository repository;

    public RefreshSessionPersistenceAdapter(RefreshSessionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<RefreshSession> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash).map(this::toDomain);
    }

    @Override
    public Optional<RefreshSession> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public RefreshSession save(RefreshSession session) {
        return toDomain(repository.save(toEntity(session)));
    }

    private RefreshSessionJpaEntity toEntity(RefreshSession session) {
        UserJpaEntity user = new UserJpaEntity();
        user.setId(session.userId());
        return new RefreshSessionJpaEntity(
                session.id(),
                user,
                session.tokenHash(),
                session.expiresAt(),
                session.createdAt(),
                session.revokedAt()
        );
    }

    private RefreshSession toDomain(RefreshSessionJpaEntity entity) {
        return new RefreshSession(
                entity.getId(),
                entity.getUser().getId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getCreatedAt(),
                entity.getRevokedAt()
        );
    }
}
