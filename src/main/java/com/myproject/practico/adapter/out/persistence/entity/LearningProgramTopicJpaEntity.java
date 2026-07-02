package com.myproject.practico.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(
        name = "learning_program_topic",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_learning_program_topic_order", columnNames = {"program_id", "order_index"}),
                @UniqueConstraint(name = "uk_learning_program_topic_topic", columnNames = {"program_id", "topic_id"})
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningProgramTopicJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private LearningProgramJpaEntity program;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private TopicJpaEntity topic;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
}
