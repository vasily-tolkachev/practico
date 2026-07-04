package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.IdentityJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.UserJpaEntity;
import com.myproject.practico.application.port.out.IdentityPersistencePort;
import com.myproject.practico.domain.AuthenticationProviderType;
import com.myproject.practico.domain.Identity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IdentityPersistenceAdapter implements IdentityPersistencePort {

    private final IdentityJpaRepository identityJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<Identity> findByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject) {
        return identityJpaRepository.findByProviderAndProviderSubject(provider, providerSubject).map(this::toDomain);
    }

    @Override
    public Identity save(Identity identity) {
        UserJpaEntity user = userJpaRepository.findById(identity.userId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + identity.userId()));
        IdentityJpaEntity saved = identityJpaRepository.save(new IdentityJpaEntity(
                identity.id(),
                user,
                identity.provider(),
                identity.providerSubject(),
                identity.email(),
                identity.displayName(),
                identity.avatarUrl(),
                identity.createdAt()
        ));
        return toDomain(saved);
    }

    @Override
    public boolean existsByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject) {
        return identityJpaRepository.existsByProviderAndProviderSubject(provider, providerSubject);
    }

    @Override
    public List<Identity> findByUserId(Long userId) {
        return identityJpaRepository.findByUser_Id(userId).stream().map(this::toDomain).toList();
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
