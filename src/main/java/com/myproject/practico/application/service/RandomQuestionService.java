package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetRandomQuestionUseCase;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.domain.Question;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomQuestionService implements GetRandomQuestionUseCase {

    private final QuestionPersistencePort questionPersistencePort;

    public RandomQuestionService(QuestionPersistencePort questionPersistencePort) {
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
}
