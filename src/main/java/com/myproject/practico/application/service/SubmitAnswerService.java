package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.out.AnswerPersistencePort;
import com.myproject.practico.application.port.out.UserPersistencePort;
import com.myproject.practico.domain.Answer;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.User;

import java.time.Instant;

public class SubmitAnswerService implements SubmitAnswerUseCase {

    private final LearningSessionService learningSessionService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final AiEvaluationService aiEvaluationService;
    private final UserPersistencePort userPersistencePort;
    private final AnswerPersistencePort answerPersistencePort;

    public SubmitAnswerService(
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase,
            AiEvaluationService aiEvaluationService,
            UserPersistencePort userPersistencePort,
            AnswerPersistencePort answerPersistencePort
    ) {
        this.learningSessionService = learningSessionService;
        this.getQuestionUseCase = getQuestionUseCase;
        this.aiEvaluationService = aiEvaluationService;
        this.userPersistencePort = userPersistencePort;
        this.answerPersistencePort = answerPersistencePort;
    }

    @Override
    public String submit(String userId, String answer) {
        if (answer == null || answer.isBlank()) {
            return "Please send a non-empty answer.";
        }

        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);

        if (session == null) {
            return "No active learning session. Send /start to begin.";
        }

        Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);

        if (currentQuestion == null) {
            return "Current question was not found. Send /start to restart.";
        }

        AiResponse aiResponse = aiEvaluationService.evaluate(currentQuestion.text(), answer);
        persistAnswer(userId, currentQuestion, answer, aiResponse);

        String nextDifficulty = learningSessionService.nextDifficulty(session, aiResponse.score());
        Question nextQuestion = getQuestionUseCase
                .getNext(nextDifficulty, learningSessionService.excludedQuestionIds(session))
                .orElse(null);

        learningSessionService.recordAnswerAndSetNextQuestion(
                userId,
                aiResponse.score(),
                nextQuestion == null ? null : nextQuestion.id()
        );

        LearningSessionStore.LearningSession updatedSession = learningSessionService.getSession(userId).orElse(session);
        return buildFinalResponse(aiResponse, nextQuestion, updatedSession);
    }

    private String buildFinalResponse(
            AiResponse aiResponse,
            Question nextQuestion,
            LearningSessionStore.LearningSession session
    ) {
        StringBuilder response = new StringBuilder();
        response.append("✅ Score: ").append(aiResponse.score()).append("/10\n\n");
        response.append("📈 Learning progress: answered ").append(session.answeredCount()).append(" questions");
        response.append(", average ").append(String.format("%.1f", learningSessionService.averageLastScores(session))).append("/10\n\n");
        response.append("💡 Feedback:\n").append(aiResponse.feedback());

        if (nextQuestion != null) {
            if (nextQuestion.concept() != null && nextQuestion.concept().topic() != null) {
                response.append("\n\n📚 Topic: ").append(nextQuestion.concept().topic().name());
                response.append("\n🧩 Concept: ").append(nextQuestion.concept().name());
            }
            response.append("\n\n❓ Next question:\n").append(nextQuestion.text());
        } else {
            response.append("\n\n🏁 No more new questions in this session. Send /start to restart.");
        }

        return response.toString();
    }

    private void persistAnswer(String userId, Question currentQuestion, String answerText, AiResponse aiResponse) {
        Instant now = Instant.now();
        User user = userPersistencePort.upsertByTelegramId(userId, now);
        answerPersistencePort.save(new Answer(
                null,
                user.id(),
                currentQuestion.id(),
                answerText,
                aiResponse.score(),
                aiResponse.feedback(),
                now
        ));
    }
}
