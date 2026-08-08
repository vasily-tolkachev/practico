package com.myproject.practico.adapter.out.persistence.entity;

import com.myproject.practico.domain.MicroConceptContentStatus;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "micro_concept_content")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicroConceptContentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private LearningProgramJpaEntity program;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "micro_concept_id", nullable = false)
    private MicroConceptJpaEntity microConcept;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MicroConceptContentStatus status;

    @Column(name = "question_payload")
    private String questionPayload;

    @Column(name = "learning_card_payload")
    private String learningCardPayload;

    @Column(name = "practice_payload")
    private String practicePayload;

    @Column(name = "quick_check_payload")
    private String quickCheckPayload;

    @Column(name = "retry_payload")
    private String retryPayload;

    @Column(name = "generated_at")
    private Instant generatedAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
