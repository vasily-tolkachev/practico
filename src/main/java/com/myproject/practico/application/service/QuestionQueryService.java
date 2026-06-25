package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.ListQuestionsUseCase;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.domain.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionQueryService implements ListQuestionsUseCase {

    private final QuestionPersistencePort questionPersistencePort;

    @Override
    public List<Question> getAll() {
        return questionPersistencePort.findAll();
    }
}
