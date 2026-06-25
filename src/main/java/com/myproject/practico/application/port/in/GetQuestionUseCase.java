package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.Question;

public interface GetQuestionUseCase {
    Question getRandom();
}
