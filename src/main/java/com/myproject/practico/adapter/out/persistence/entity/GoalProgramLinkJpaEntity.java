package com.myproject.practico.adapter.out.persistence.entity;

import com.myproject.practico.domain.GoalProgramSourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "goal_program_link",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_goal_program_link_goal", columnNames = {"goal_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalProgramLinkJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "program_id", nullable = false, length = 128)
    private String programId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 32)
    private GoalProgramSourceType sourceType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
