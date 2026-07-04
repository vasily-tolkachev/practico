package com.myproject.practico.auth.adapter.out.persistence;

import com.myproject.practico.auth.adapter.out.persistence.entity.IdentityJpaEntity;
import com.myproject.practico.auth.domain.AuthenticationProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdentityJpaRepository extends JpaRepository<IdentityJpaEntity, UUID> {
    Optional<IdentityJpaEntity> findByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject);
}
