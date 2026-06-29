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
                    .getNextFromNextConcept(null, learningSessionService.firstDifficulty(), Set.of())
                    .orElseThrow(() -> new IllegalStateException("No questions found"));

            Long conceptId = question.concept() == null ? null : question.concept().id();
            learningSessionService.startLearningSession(userId, conceptId, question.id());
            return buildResponse(question);
        } catch (IllegalStateException ex) {
            return "No questions are available yet.";
        }
    }

    private String buildResponse(Question question) {
        StringBuilder response = new StringBuilder();
        response.append("Learning started.\n\n");

        if (question.concept() != null && question.concept().id() != null) {
            int order = getQuestionUseCase.conceptOrder(question.concept().id());
            int total = getQuestionUseCase.totalConcepts();
            if (order > 0 && total > 0) {
                response.append("Progress: Concept ").append(order).append(" of ").append(total);
                if (question.microConcept() != null && question.microConcept().id() != null) {
                    int microOrder = getQuestionUseCase.microConceptOrder(question.concept().id(), question.microConcept().id());
                    int microTotal = getQuestionUseCase.totalMicroConcepts(question.concept().id());
                    if (microOrder > 0 && microTotal > 0) {
                        response.append(" | Micro ").append(microOrder).append(" of ").append(microTotal);
                    }
                }
                response.append("\n\n");
            }
        }

        response.append("Question:\n").append(question.text());

        if (question.concept() != null && question.concept().topic() != null) {
            response.append("\n\nTopic: ").append(question.concept().topic().name());
            response.append("\nConcept: ").append(question.concept().name());
        }

        if (question.difficulty() != null) {
            response.append("\nDifficulty: ").append(question.difficulty());
        }

        return response.toString();
    }
}
