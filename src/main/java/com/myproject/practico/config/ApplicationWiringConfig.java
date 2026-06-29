package com.myproject.practico.config;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.HandleIncomingMessageUseCase;
import com.myproject.practico.application.port.in.StartLearningUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.port.out.AnswerPersistencePort;
import com.myproject.practico.application.port.out.CommandInterpreterPort;
import com.myproject.practico.application.port.out.EvaluationPort;
import com.myproject.practico.application.port.out.MessengerPort;
import com.myproject.practico.application.port.out.QuickCheckPort;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.application.port.out.UserConceptProgressPersistencePort;
import com.myproject.practico.application.port.out.UserPersistencePort;
import com.myproject.practico.application.service.EvaluationService;
import com.myproject.practico.application.service.DefaultLearningEngine;
import com.myproject.practico.application.service.DefaultRetryMasteryPolicy;
import com.myproject.practico.application.service.GetQuestionService;
import com.myproject.practico.application.service.HandleIncomingMessageService;
import com.myproject.practico.application.service.LearningEngine;
import com.myproject.practico.application.service.LearningSessionService;
import com.myproject.practico.application.service.RetryMasteryPolicy;
import com.myproject.practico.application.service.StartLearningService;
import com.myproject.practico.application.service.SubmitAnswerService;
import com.myproject.practico.application.service.LearningSessionStore;
import com.myproject.practico.application.service.QuickCheckService;
import com.myproject.practico.application.service.UserConceptProgressService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationWiringConfig {

    @Bean
    public GetQuestionUseCase getQuestionUseCase(QuestionPersistencePort questionPersistencePort) {
        return new GetQuestionService(questionPersistencePort);
    }

    @Bean
    public LearningSessionStore learningSessionStore() {
        return new LearningSessionStore();
    }

    @Bean
    public EvaluationService evaluationService(EvaluationPort evaluationPort) {
        return new EvaluationService(evaluationPort);
    }

    @Bean
    public LearningSessionService learningSessionService(LearningSessionStore learningSessionStore) {
        return new LearningSessionService(learningSessionStore);
    }

    @Bean
    public UserConceptProgressService userConceptProgressService(
            UserConceptProgressPersistencePort userConceptProgressPersistencePort
    ) {
        return new UserConceptProgressService(userConceptProgressPersistencePort);
    }

    @Bean
    public QuickCheckService quickCheckService(QuickCheckPort quickCheckPort) {
        return new QuickCheckService(quickCheckPort);
    }

    @Bean
    public RetryMasteryPolicy retryMasteryPolicy() {
        return new DefaultRetryMasteryPolicy();
    }

    @Bean
    public LearningEngine learningEngine(
            EvaluationService evaluationService,
            UserConceptProgressService userConceptProgressService,
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService,
            RetryMasteryPolicy retryMasteryPolicy
    ) {
        return new DefaultLearningEngine(
                evaluationService,
                userConceptProgressService,
                getQuestionUseCase,
                learningSessionService,
                retryMasteryPolicy
        );
    }

    @Bean
    public StartLearningUseCase startLearningUseCase(
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService
    ) {
        return new StartLearningService(getQuestionUseCase, learningSessionService);
    }

    @Bean
    public SubmitAnswerUseCase submitAnswerUseCase(
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase,
            LearningEngine learningEngine,
            UserPersistencePort userPersistencePort,
            AnswerPersistencePort answerPersistencePort
    ) {
        return new SubmitAnswerService(
                learningSessionService,
                getQuestionUseCase,
                learningEngine,
                userPersistencePort,
                answerPersistencePort
        );
    }

    @Bean
    public HandleIncomingMessageUseCase handleIncomingMessageUseCase(
            CommandInterpreterPort commandInterpreterPort,
            SubmitAnswerUseCase submitAnswerUseCase,
            GetQuestionUseCase getQuestionUseCase,
            LearningSessionService learningSessionService,
            QuickCheckService quickCheckService,
            MessengerPort messengerPort
    ) {
        return new HandleIncomingMessageService(
                commandInterpreterPort,
                submitAnswerUseCase,
                getQuestionUseCase,
                learningSessionService,
                quickCheckService,
                messengerPort
        );
    }
}
