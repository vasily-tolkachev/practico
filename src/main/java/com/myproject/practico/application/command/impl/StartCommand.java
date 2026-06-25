package com.myproject.practico.application.command.impl;

import com.myproject.practico.application.command.Command;
import com.myproject.practico.application.port.in.GetRandomQuestionUseCase;
import com.myproject.practico.domain.Question;

public class StartCommand implements Command {

    private final GetRandomQuestionUseCase getRandomQuestionUseCase;

    public StartCommand(GetRandomQuestionUseCase getRandomQuestionUseCase) {
        this.getRandomQuestionUseCase = getRandomQuestionUseCase;
    }

    @Override
    public boolean supports(String text) {
        return "/start".equalsIgnoreCase(text);
    }

    @Override
    public String handle(String userId, String text) {
        try {
            Question question = getRandomQuestionUseCase.getRandom();
            return buildResponse(question);
        } catch (IllegalStateException ex) {
            return "No questions are available yet.";
        }
    }

    private String buildResponse(Question question) {
        StringBuilder response = new StringBuilder();
        response.append("Random question:\n");
        response.append(question.text());

        if (question.topic() != null && !question.topic().isBlank()) {
            response.append("\nTopic: ").append(question.topic());
        }

        if (question.difficulty() != null && !question.difficulty().isBlank()) {
            response.append("\nDifficulty: ").append(question.difficulty());
        }

        return response.toString();
    }
}
