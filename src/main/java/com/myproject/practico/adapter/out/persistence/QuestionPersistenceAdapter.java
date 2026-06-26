package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.QuestionJpaEntity;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.domain.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Question> findById(Long id) {
        return questionJpaRepository.findById(id)
                .map(this::toDomain);
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
