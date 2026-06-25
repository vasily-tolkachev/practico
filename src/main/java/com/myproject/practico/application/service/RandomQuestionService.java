package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetRandomQuestionUseCase;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.domain.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class RandomQuestionService implements GetRandomQuestionUseCase {

    private final QuestionPersistencePort questionPersistencePort;

    @Override
    public Question getRandom() {
        List<Question> questions = questionPersistencePort.findAll();

        if (questions.isEmpty()) {
            throw new IllegalStateException("No questions found");
        }

        return questions.get(ThreadLocalRandom.current().nextInt(questions.size()));
    }
}
