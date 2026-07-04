package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.AnswerJpaEntity;
import com.myproject.practico.adapter.out.persistence.entity.UserJpaEntity;
import com.myproject.practico.application.port.out.AnswerPersistencePort;
import com.myproject.practico.domain.Answer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AnswerPersistenceAdapter implements AnswerPersistencePort {

    private final AnswerJpaRepository answerJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public void save(Answer answer) {
        UserJpaEntity user = userJpaRepository.findById(answer.userId())
                .orElseThrow(() -> new IllegalStateException("User not found for answer persistence"));

        answerJpaRepository.save(new AnswerJpaEntity(
                null,
                user,
                answer.questionId(),
                answer.answer(),
                answer.score(),
                answer.feedback(),
                answer.createdAt()
        ));
    }
}
