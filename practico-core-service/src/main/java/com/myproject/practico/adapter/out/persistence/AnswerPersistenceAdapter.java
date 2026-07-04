package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.AnswerJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.LearningProfileJpaEntity;
import com.myproject.practico.application.port.out.AnswerPersistencePort;
import com.myproject.practico.domain.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AnswerPersistenceAdapter implements AnswerPersistencePort {

    private final AnswerJpaRepository answerJpaRepository;
    private final LearningProfileJpaRepository learningProfileJpaRepository;

    @Override
    public void save(Answer answer) {
        UUID profileId = answer.profileId();
        LearningProfileJpaEntity profile = learningProfileJpaRepository.findById(profileId)
                .orElseThrow(() -> new IllegalStateException("Learning profile not found for answer persistence"));

        answerJpaRepository.save(new AnswerJpaEntity(
                null,
                profile,
                answer.questionId(),
                answer.answer(),
                answer.score(),
                answer.feedback(),
                answer.createdAt()
        ));
    }
}
