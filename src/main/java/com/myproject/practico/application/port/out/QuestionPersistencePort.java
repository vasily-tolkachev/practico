package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.Question;

import java.util.List;

public interface QuestionPersistencePort {
    List<Question> findAll();
}
