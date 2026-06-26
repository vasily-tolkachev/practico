package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.domain.Question;

public class SubmitAnswerService implements SubmitAnswerUseCase {

    private final UserSessionStore userSessionStore;
    private final GetQuestionUseCase getQuestionUseCase;
    private final AiEvaluationService aiEvaluationService;

    public SubmitAnswerService(
            UserSessionStore userSessionStore,
            GetQuestionUseCase getQuestionUseCase,
            AiEvaluationService aiEvaluationService
    ) {
        this.userSessionStore = userSessionStore;
        this.getQuestionUseCase = getQuestionUseCase;
        this.aiEvaluationService = aiEvaluationService;
    }

    @Override
    public String submit(String userId, String answer) {
        if (answer == null || answer.isBlank()) {
            return "Please send a non-empty answer.";
        }

        UserSessionStore.UserSession session = userSessionStore.get(userId).orElse(null);

        if (session == null) {
            return "No active interview. Send /start to begin.";
        }

        Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);

        if (currentQuestion == null) {
            return "Current question was not found. Send /start to restart.";
        }

        AiResponse aiResponse = aiEvaluationService.evaluate(currentQuestion.text(), answer);

        Question nextQuestion;
        try {
            nextQuestion = getQuestionUseCase.getRandom();
        } catch (IllegalStateException ex) {
            return buildFinalResponse(aiResponse, null);
        }

        userSessionStore.put(userId, nextQuestion.id());
        return buildFinalResponse(aiResponse, nextQuestion);
    }

    private String buildFinalResponse(AiResponse aiResponse, Question nextQuestion) {
        StringBuilder response = new StringBuilder();
        response.append("Score: ").append(aiResponse.score()).append("/10\n\n");
        response.append("Feedback:\n").append(aiResponse.feedback());

        if (nextQuestion != null) {
            response.append("\n\nNext question:\n").append(nextQuestion.text());
        }

        return response.toString();
    }
}
