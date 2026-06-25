package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.StartInterviewUseCase;
import com.myproject.practico.domain.Question;

public class StartInterviewService implements StartInterviewUseCase {

    private final GetQuestionUseCase getQuestionUseCase;

    public StartInterviewService(GetQuestionUseCase getQuestionUseCase) {
        this.getQuestionUseCase = getQuestionUseCase;
    }

    @Override
    public String start(String userId) {
        try {
            Question question = getQuestionUseCase.getRandom();
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
