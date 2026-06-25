package com.myproject.practico.service;

import com.myproject.practico.model.Question;
import com.myproject.practico.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class QuestionService {

    private final QuestionRepository repo;
    private final Random random = new Random();

    public QuestionService(QuestionRepository repo) {
        this.repo = repo;
    }

    public Question getRandomQuestion() {
        List<Question> questions = repo.findAll();

        if (questions.isEmpty()) {
            throw new IllegalStateException("No questions found");
        }

        return questions.get(random.nextInt(questions.size()));
    }
}