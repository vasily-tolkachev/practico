package com.myproject.practico.auth.adapter.out.persistence;

import com.myproject.practico.auth.adapter.out.persistence.entity.IdentityJpaEntity;
import com.myproject.practico.auth.adapter.out.persistence.entity.UserJpaEntity;
import com.myproject.practico.auth.application.port.IdentityRepository;
import com.myproject.practico.auth.domain.AuthenticationProviderType;
import com.myproject.practico.auth.domain.Identity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class IdentityPersistenceAdapter implements IdentityRepository {

    private final IdentityJpaRepository repository;

    public IdentityPersistenceAdapter(IdentityJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Identity> findByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject) {
        return repository.findByProviderAndProviderSubject(provider, providerSubject).map(this::toDomain);
    }

    @Override
    public Optional<Identity> findFirstByUserId(UUID userId) {
        return repository.findFirstByUser_IdOrderByCreatedAtAsc(userId).map(this::toDomain);
    }

    @Override
    public Identity save(Identity identity) {
        return toDomain(repository.save(toEntity(identity)));
    }

    private IdentityJpaEntity toEntity(Identity identity) {
        UserJpaEntity user = new UserJpaEntity();
        user.setId(identity.userId());
        return new IdentityJpaEntity(
                identity.id(),
                user,
                identity.provider(),
                identity.providerSubject(),
                identity.email(),
                identity.displayName(),
                identity.avatarUrl(),
                identity.createdAt()
        );
    }

    private Identity toDomain(IdentityJpaEntity entity) {
        return new Identity(
                entity.getId(),
                entity.getUser().getId(),
                entity.getProvider(),
                entity.getProviderSubject(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getAvatarUrl(),
                entity.getCreatedAt()
        );
    }
}
