package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.QuestionJpaEntity;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.domain.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuestionPersistenceAdapter implements QuestionPersistencePort {

    private final QuestionJpaRepository questionJpaRepository;

    @Override
    public List<Question> findAll() {
        return questionJpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Question toDomain(QuestionJpaEntity entity) {
        return new Question(
                entity.getId(),
                entity.getText(),
                entity.getTopic(),
                entity.getDifficulty()
        );
    }
}
