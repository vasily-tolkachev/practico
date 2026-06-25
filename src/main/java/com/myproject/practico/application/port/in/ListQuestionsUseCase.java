package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.Question;

import java.util.List;

public interface ListQuestionsUseCase {
    List<Question> getAll();
}
