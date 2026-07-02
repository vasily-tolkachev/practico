package com.myproject.practico.adapter.out.persistence.entity;

import com.myproject.practico.domain.LearningProgramOrigin;
import com.myproject.practico.domain.LearningProgramStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "learning_program")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgramJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LearningProgramStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LearningProgramOrigin origin;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}
