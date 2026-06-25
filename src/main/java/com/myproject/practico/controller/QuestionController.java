package com.myproject.practico.controller;

import com.myproject.practico.model.Question;
import com.myproject.practico.repository.QuestionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QuestionController {

    private final QuestionRepository questionRepository;

    public QuestionController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping("/test")
    public List<Question> test() {
        return questionRepository.findAll();
    }
}