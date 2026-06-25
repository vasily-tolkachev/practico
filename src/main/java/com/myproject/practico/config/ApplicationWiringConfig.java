package com.myproject.practico.config;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.HandleIncomingMessageUseCase;
import com.myproject.practico.application.port.in.StartInterviewUseCase;
import com.myproject.practico.application.port.out.CommandInterpreterPort;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.application.service.GetQuestionService;
import com.myproject.practico.application.service.HandleIncomingMessageService;
import com.myproject.practico.application.service.StartInterviewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationWiringConfig {

    @Bean
    public GetQuestionUseCase getQuestionUseCase(QuestionPersistencePort questionPersistencePort) {
        return new GetQuestionService(questionPersistencePort);
    }

    @Bean
    public StartInterviewUseCase startInterviewUseCase(GetQuestionUseCase getQuestionUseCase) {
        return new StartInterviewService(getQuestionUseCase);
    }

    @Bean
    public HandleIncomingMessageUseCase handleIncomingMessageUseCase(
            CommandInterpreterPort commandInterpreterPort,
            MessengerPort messengerPort
    ) {
        return new HandleIncomingMessageService(commandInterpreterPort, messengerPort);
    }
}
