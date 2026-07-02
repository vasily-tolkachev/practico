package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.MicroConceptJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.QuestionJpaEntity;
import com.myproject.practico.application.port.out.GeneratedQuestionPersistencePort;
import com.myproject.practico.application.program.GeneratedQuestion;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GeneratedQuestionPersistenceAdapter implements GeneratedQuestionPersistencePort {

    private final QuestionJpaRepository questionJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Long microConceptId, List<GeneratedQuestion> questions) {
        if (microConceptId == null || microConceptId <= 0 || questions == null || questions.isEmpty()) {
            return;
        }

        MicroConceptJpaEntity microConceptRef = entityManager.getReference(MicroConceptJpaEntity.class, microConceptId);
        for (GeneratedQuestion question : questions) {
            if (question == null || question.text() == null || question.text().isBlank()) {
                continue;
            }
            questionJpaRepository.save(new QuestionJpaEntity(
                    null,
                    microConceptRef,
                    question.text().trim(),
                    question.difficulty(),
                    question.questionType(),
                    question.expectedAnswer(),
                    question.explanation()
            ));
        }
    }
}
