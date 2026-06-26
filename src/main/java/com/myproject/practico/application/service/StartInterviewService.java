package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.StartInterviewUseCase;
import com.myproject.practico.domain.Question;

import java.util.Set;

public class StartInterviewService implements StartInterviewUseCase {

    private final GetQuestionUseCase getQuestionUseCase;
    private final SessionService sessionService;

    public StartInterviewService(
            GetQuestionUseCase getQuestionUseCase,
            SessionService sessionService
    ) {
        this.getQuestionUseCase = getQuestionUseCase;
        this.sessionService = sessionService;
    }

    @Override
    public String start(String userId) {
        try {
            Question question = getQuestionUseCase
                    .getNext(sessionService.firstDifficulty(), Set.of())
                    .orElseThrow(() -> new IllegalStateException("No questions found"));

            sessionService.startSession(userId, question.id());
            return buildResponse(question);
        } catch (IllegalStateException ex) {
            return "No questions are available yet.";
        }
    }

    private String buildResponse(Question question) {
        StringBuilder response = new StringBuilder();
        response.append("🚀 Interview started.\n\n");
        response.append("❓ Question:\n").append(question.text());

        if (question.topic() != null && !question.topic().isBlank()) {
            response.append("\n\n📚 Topic: ").append(question.topic());
        }

        if (question.difficulty() != null && !question.difficulty().isBlank()) {
            response.append("\n🎯 Difficulty: ").append(question.difficulty());
        }

        return response.toString();
    }
}
