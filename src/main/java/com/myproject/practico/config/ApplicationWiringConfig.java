package com.myproject.practico.config;

import com.myproject.practico.application.command.Command;
import com.myproject.practico.application.command.CommandRouter;
import com.myproject.practico.application.command.impl.HelpCommand;
import com.myproject.practico.application.command.impl.StartCommand;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.application.service.MessageService;
import com.myproject.practico.application.service.RandomQuestionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ApplicationWiringConfig {

    @Bean
    public HelpCommand helpCommand() {
        return new HelpCommand();
    }

    @Bean
    public StartCommand startCommand(RandomQuestionService randomQuestionService) {
        return new StartCommand(randomQuestionService);
    }

    @Bean
    public CommandRouter commandRouter(List<Command> commands) {
        return new CommandRouter(commands);
    }

    @Bean
    public RandomQuestionService randomQuestionService(QuestionPersistencePort questionPersistencePort) {
        return new RandomQuestionService(questionPersistencePort);
    }

    @Bean
    public MessageService messageService(CommandRouter commandRouter, MessengerPort messengerPort) {
        return new MessageService(commandRouter, messengerPort);
    }
}
