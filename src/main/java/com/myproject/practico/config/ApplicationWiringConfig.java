package com.myproject.practico.config;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.HandleIncomingMessageUseCase;
import com.myproject.practico.application.port.in.StartInterviewUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.out.AiEvaluationPort;
import com.myproject.practico.application.port.out.CommandInterpreterPort;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.application.service.AiEvaluationService;
import com.myproject.practico.application.service.GetQuestionService;
import com.myproject.practico.application.service.HandleIncomingMessageService;
import com.myproject.practico.application.service.SessionService;
import com.myproject.practico.application.service.StartInterviewService;
import com.myproject.practico.application.service.SubmitAnswerService;
import com.myproject.practico.application.service.UserSessionStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationWiringConfig {

    @Bean
    public GetQuestionUseCase getQuestionUseCase(QuestionPersistencePort questionPersistencePort) {
        return new GetQuestionService(questionPersistencePort);
    }

    @Bean
    public UserSessionStore userSessionStore() {
        return new UserSessionStore();
    }

    @Bean
    public AiEvaluationService aiEvaluationService(AiEvaluationPort aiEvaluationPort) {
        return new AiEvaluationService(aiEvaluationPort);
    }

    @Bean
    public SessionService sessionService(UserSessionStore userSessionStore) {
        return new SessionService(userSessionStore);
    }

    @Bean
    public StartInterviewUseCase startInterviewUseCase(
            GetQuestionUseCase getQuestionUseCase,
            SessionService sessionService
    ) {
        return new StartInterviewService(getQuestionUseCase, sessionService);
    }

    @Bean
    public SubmitAnswerUseCase submitAnswerUseCase(
            SessionService sessionService,
            GetQuestionUseCase getQuestionUseCase,
            AiEvaluationService aiEvaluationService
    ) {
        return new SubmitAnswerService(
                sessionService,
                getQuestionUseCase,
                aiEvaluationService
        );
    }

    @Bean
    public HandleIncomingMessageUseCase handleIncomingMessageUseCase(
            CommandInterpreterPort commandInterpreterPort,
            SubmitAnswerUseCase submitAnswerUseCase,
            MessengerPort messengerPort
    ) {
        return new HandleIncomingMessageService(
                commandInterpreterPort,
                submitAnswerUseCase,
                messengerPort
        );
    }
}
