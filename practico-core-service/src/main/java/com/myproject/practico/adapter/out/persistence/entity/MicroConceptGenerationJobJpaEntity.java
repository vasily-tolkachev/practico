package com.myproject.practico.adapter.out.persistence.entity;

import com.myproject.practico.domain.MicroConceptGenerationJobStatus;
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
@Table(name = "micro_concept_generation_job")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MicroConceptGenerationJobJpaEntity {

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
    private MicroConceptGenerationJobStatus status;

    @Column(name = "progress_percent", nullable = false)
    private Integer progressPercent;

    @Column(name = "status_message", length = 2000)
    private String statusMessage;

    @Column(name = "requested_by", length = 128)
    private String requestedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
