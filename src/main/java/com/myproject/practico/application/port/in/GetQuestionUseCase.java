package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.Question;

import java.util.Optional;

public interface GetQuestionUseCase {
    Question getRandom();

    Optional<Question> getById(Long id);
}
