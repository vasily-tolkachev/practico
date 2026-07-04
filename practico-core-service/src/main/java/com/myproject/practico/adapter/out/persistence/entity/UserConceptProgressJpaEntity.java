package com.myproject.practico.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        name = "user_concept_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "concept_id"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConceptProgressJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id", nullable = false)
    private LearningProfileJpaEntity profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    private ConceptJpaEntity concept;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatusJpa status;

    @Column(nullable = false)
    private int correctAnswers;

    @Column(nullable = false)
    private int totalAnswers;

    @Column(nullable = false)
    private Instant updatedAt;
}
