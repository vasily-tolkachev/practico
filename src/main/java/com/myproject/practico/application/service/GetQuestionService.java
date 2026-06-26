package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.domain.Question;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class GetQuestionService implements GetQuestionUseCase {

    private final QuestionPersistencePort questionPersistencePort;

    public GetQuestionService(QuestionPersistencePort questionPersistencePort) {
        this.questionPersistencePort = questionPersistencePort;
    }

    @Override
    public Question getRandom() {
        List<Question> questions = questionPersistencePort.findAll();

        if (questions.isEmpty()) {
            throw new IllegalStateException("No questions found");
        }

        return questions.get(ThreadLocalRandom.current().nextInt(questions.size()));
    }

    @Override
    public Optional<Question> getById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        return questionPersistencePort.findById(id);
    }
}
