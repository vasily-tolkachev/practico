package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.StartLearningUseCase;
import com.myproject.practico.domain.Question;

import java.util.Set;

public class StartLearningService implements StartLearningUseCase {

    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningSessionService learningSessionService;

    public StartLearningService(
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService
    ) {
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningSessionService = learningSessionService;
    }

    @Override
    public String start(String userId) {
        try {
            Question question = getQuestionUseCase
                    .getNext(learningSessionService.firstDifficulty(), Set.of())
                    .orElseThrow(() -> new IllegalStateException("No questions found"));

            learningSessionService.startLearningSession(userId, question.id());
            return buildResponse(question);
        } catch (IllegalStateException ex) {
            return "No questions are available yet.";
        }
    }

    private String buildResponse(Question question) {
        StringBuilder response = new StringBuilder();
        response.append("🚀 Learning started.\n\n");
        response.append("❓ Question:\n").append(question.text());

        if (question.concept() != null && question.concept().topic() != null) {
            response.append("\n\n📚 Topic: ").append(question.concept().topic().name());
            response.append("\n🧩 Concept: ").append(question.concept().name());
        }

        if (question.difficulty() != null && !question.difficulty().isBlank()) {
            response.append("\n🎯 Difficulty: ").append(question.difficulty());
        }

        return response.toString();
    }
}
