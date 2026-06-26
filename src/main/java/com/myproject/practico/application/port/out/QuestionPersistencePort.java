package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.Question;

import java.util.List;
import java.util.Optional;

public interface QuestionPersistencePort {
    List<Question> findAll();

    Optional<Question> findById(Long id);
}
