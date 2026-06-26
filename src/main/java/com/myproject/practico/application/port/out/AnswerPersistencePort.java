package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.Answer;

public interface AnswerPersistencePort {
    void save(Answer answer);
}
