package com.myproject.practico.adapter.out.persistence.entity;

import com.myproject.practico.domain.AuthenticationProviderType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "identity",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_identity_provider_subject", columnNames = {"provider", "provider_subject"})
        },
        indexes = {
                @Index(name = "idx_identity_user_id", columnList = "user_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AuthenticationProviderType provider;

    @Column(name = "provider_subject", nullable = false, length = 255)
    private String providerSubject;

    @Column
    private String email;

    @Column(nullable = false)
    private String displayName;

    @Column
    private String avatarUrl;

    @Column(nullable = false)
    private Instant createdAt;
}
