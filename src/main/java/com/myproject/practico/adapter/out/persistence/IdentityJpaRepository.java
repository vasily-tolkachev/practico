package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.IdentityJpaEntity;
import com.myproject.practico.domain.AuthenticationProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IdentityJpaRepository extends JpaRepository<IdentityJpaEntity, Long> {
    Optional<IdentityJpaEntity> findByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject);
    boolean existsByProviderAndProviderSubject(AuthenticationProviderType provider, String providerSubject);
    List<IdentityJpaEntity> findByUser_Id(Long userId);
}
