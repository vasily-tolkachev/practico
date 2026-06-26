package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.domain.Question;

public class SubmitAnswerService implements SubmitAnswerUseCase {

    private final SessionService sessionService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final AiEvaluationService aiEvaluationService;

    public SubmitAnswerService(
            SessionService sessionService,
            GetQuestionUseCase getQuestionUseCase,
            AiEvaluationService aiEvaluationService
    ) {
        this.sessionService = sessionService;
        this.getQuestionUseCase = getQuestionUseCase;
        this.aiEvaluationService = aiEvaluationService;
    }

    @Override
    public String submit(String userId, String answer) {
        if (answer == null || answer.isBlank()) {
            return "Please send a non-empty answer.";
        }

        UserSessionStore.UserSession session = sessionService.getSession(userId).orElse(null);

        if (session == null) {
            return "No active interview. Send /start to begin.";
        }

        Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);

        if (currentQuestion == null) {
            return "Current question was not found. Send /start to restart.";
        }

        AiResponse aiResponse = aiEvaluationService.evaluate(currentQuestion.text(), answer);

        String nextDifficulty = sessionService.nextDifficulty(session, aiResponse.score());
        Question nextQuestion = getQuestionUseCase
                .getNext(nextDifficulty, sessionService.excludedQuestionIds(session))
                .orElse(null);

        sessionService.recordAnswerAndSetNextQuestion(
                userId,
                aiResponse.score(),
                nextQuestion == null ? null : nextQuestion.id()
        );

        UserSessionStore.UserSession updatedSession = sessionService.getSession(userId).orElse(session);
        return buildFinalResponse(aiResponse, nextQuestion, updatedSession);
    }

    private String buildFinalResponse(
            AiResponse aiResponse,
            Question nextQuestion,
            UserSessionStore.UserSession session
    ) {
        StringBuilder response = new StringBuilder();
        response.append("✅ Score: ").append(aiResponse.score()).append("/10\n\n");
        response.append("📈 Progress: answered ").append(session.answeredCount()).append(" questions");
        response.append(", average ").append(String.format("%.1f", sessionService.averageLastScores(session))).append("/10\n\n");
        response.append("💡 Feedback:\n").append(aiResponse.feedback());

        if (nextQuestion != null) {
            if (nextQuestion.topic() != null && !nextQuestion.topic().isBlank()) {
                response.append("\n\n📚 Topic: ").append(nextQuestion.topic());
            }
            response.append("\n\n❓ Next question:\n").append(nextQuestion.text());
        } else {
            response.append("\n\n🏁 No more new questions in this session. Send /start to restart.");
        }

        return response.toString();
    }
}
