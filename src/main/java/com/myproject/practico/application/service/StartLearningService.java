package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.StartLearningUseCase;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.Question;

import java.util.Set;

public class StartLearningService implements StartLearningUseCase {

    private static final String DEFAULT_CONCEPT_NAME = "\u0417\u0432\u0451\u0437\u0434\u044b";
    private static final String DEFAULT_TOPIC_NAME = "\u041a\u043e\u0441\u043c\u043e\u0441";

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
                    .getFirstFromConceptName(DEFAULT_CONCEPT_NAME, learningSessionService.firstDifficulty(), Set.of())
                    .or(() -> getQuestionUseCase.getFirstFromTopic(DEFAULT_TOPIC_NAME, learningSessionService.firstDifficulty(), Set.of()))
                    .or(() -> getQuestionUseCase.getNextFromNextConcept(null, learningSessionService.firstDifficulty(), Set.of()))
                    .orElseThrow(() -> new IllegalStateException("\u0412\u043e\u043f\u0440\u043e\u0441\u044b \u043d\u0435 \u043d\u0430\u0439\u0434\u0435\u043d\u044b"));

            Long conceptId = question.concept() == null ? null : question.concept().id();
            learningSessionService.startLearningSession(userId, conceptId, question.id());
            return buildResponse(question);
        } catch (IllegalStateException ex) {
            return "\u041f\u043e\u043a\u0430 \u043d\u0435\u0442 \u0434\u043e\u0441\u0442\u0443\u043f\u043d\u044b\u0445 \u0432\u043e\u043f\u0440\u043e\u0441\u043e\u0432.";
        }
    }

    private String buildResponse(Question question) {
        StringBuilder response = new StringBuilder();
        response.append("\u041e\u0431\u0443\u0447\u0435\u043d\u0438\u0435 \u043d\u0430\u0447\u0430\u043b\u043e\u0441\u044c.\n\n");

        if (question.concept() != null && question.concept().id() != null) {
            int order = getQuestionUseCase.conceptOrder(question.concept().id());
            int total = getQuestionUseCase.totalConcepts();
            if (order > 0 && total > 0) {
                response.append("\u041f\u0440\u043e\u0433\u0440\u0435\u0441\u0441: \u041a\u043e\u043d\u0446\u0435\u043f\u0442 ").append(order).append(" \u0438\u0437 ").append(total);
                if (question.microConcept() != null && question.microConcept().id() != null) {
                    int microOrder = getQuestionUseCase.microConceptOrder(question.concept().id(), question.microConcept().id());
                    int microTotal = getQuestionUseCase.totalMicroConcepts(question.concept().id());
                    if (microOrder > 0 && microTotal > 0) {
                        response.append(" | \u041c\u0438\u043a\u0440\u043e ").append(microOrder).append(" \u0438\u0437 ").append(microTotal);
                    }
                }
                response.append("\n\n");
            }
        }

        response.append("\u0412\u043e\u043f\u0440\u043e\u0441:\n").append(question.text());

        if (question.concept() != null && question.concept().topic() != null) {
            response.append("\n\n\u0422\u0435\u043c\u0430: ").append(question.concept().topic().name());
            response.append("\n\u041a\u043e\u043d\u0446\u0435\u043f\u0442: ").append(question.concept().name());
        }

        if (question.difficulty() != null) {
            response.append("\n\u0421\u043b\u043e\u0436\u043d\u043e\u0441\u0442\u044c: ").append(formatDifficulty(question.difficulty()));
        }

        return response.toString();
    }

    private String formatDifficulty(Difficulty difficulty) {
        return switch (difficulty) {
            case EASY -> "\u041b\u0451\u0433\u043a\u0430\u044f";
            case MEDIUM -> "\u0421\u0440\u0435\u0434\u043d\u044f\u044f";
            case HARD -> "\u0421\u043b\u043e\u0436\u043d\u0430\u044f";
        };
    }
}
