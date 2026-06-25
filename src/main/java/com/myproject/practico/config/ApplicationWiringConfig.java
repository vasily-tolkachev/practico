package com.myproject.practico.config;

import com.myproject.practico.application.port.in.SendMessageUseCase;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.application.service.SendMessageService;
import com.myproject.practico.application.service.RandomQuestionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationWiringConfig {

    @Bean
    public RandomQuestionService randomQuestionService(QuestionPersistencePort questionPersistencePort) {
        return new RandomQuestionService(questionPersistencePort);
    }

    @Bean
    public SendMessageUseCase sendMessageUseCase(MessengerPort messengerPort) {
        return new SendMessageService(messengerPort);
    }
}
