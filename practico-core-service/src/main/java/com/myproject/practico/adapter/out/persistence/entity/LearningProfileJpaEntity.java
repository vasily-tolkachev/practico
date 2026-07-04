package com.myproject.practico.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "learning_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningProfileJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
