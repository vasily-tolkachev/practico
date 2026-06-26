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
    public StartInterviewUseCase startInterviewUseCase(
            GetQuestionUseCase getQuestionUseCase,
            UserSessionStore userSessionStore
    ) {
        return new StartInterviewService(getQuestionUseCase, userSessionStore);
    }

    @Bean
    public SubmitAnswerUseCase submitAnswerUseCase(
            UserSessionStore userSessionStore,
            GetQuestionUseCase getQuestionUseCase,
            AiEvaluationService aiEvaluationService
    ) {
        return new SubmitAnswerService(
                userSessionStore,
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
