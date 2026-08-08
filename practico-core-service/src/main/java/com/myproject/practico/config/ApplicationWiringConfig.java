package com.myproject.practico.config;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.GetCurrentProgramUseCase;
import com.myproject.practico.application.port.in.GetGenerationMetricsUseCase;
import com.myproject.practico.application.port.in.GetMicroConceptGenerationStatusUseCase;
import com.myproject.practico.application.port.in.GetProgramByIdUseCase;
import com.myproject.practico.application.port.in.GetLearningStateUseCase;
import com.myproject.practico.application.port.in.GetProgramStatusUseCase;
import com.myproject.practico.application.port.in.GetProgramTreeUseCase;
import com.myproject.practico.application.port.in.CreateGoalUseCase;
import com.myproject.practico.application.port.in.AttachProgramToGoalUseCase;
import com.myproject.practico.application.port.in.ListGoalsUseCase;
import com.myproject.practico.application.port.in.GetGoalUseCase;
import com.myproject.practico.application.port.in.GetGoalProgramUseCase;
import com.myproject.practico.application.port.in.GetGoalResolutionStatusUseCase;
import com.myproject.practico.application.port.in.ProgramResolverUseCase;
import com.myproject.practico.application.port.in.StartLearningFromGoalUseCase;
import com.myproject.practico.application.port.in.ContinueLearningUseCase;
import com.myproject.practico.application.port.in.GenerateMicroConceptContentUseCase;
import com.myproject.practico.application.port.in.StartLearningUseCase;
import com.myproject.practico.application.port.in.SubmitPracticeUseCase;
import com.myproject.practico.application.port.in.SubmitQuickCheckUseCase;
import com.myproject.practico.application.port.in.SubmitRetryUseCase;
import com.myproject.practico.application.port.in.SubmitAnswerUseCase;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.out.AnswerPersistencePort;
import com.myproject.practico.application.port.out.AiCourseGeneratorPort;
import com.myproject.practico.application.port.out.AiQuestionGeneratorPort;
import com.myproject.practico.application.port.out.EvaluationPort;
import com.myproject.practico.application.port.out.GenerationMetricsPort;
import com.myproject.practico.application.port.out.GeneratedQuestionPersistencePort;
import com.myproject.practico.application.port.out.GoalPersistencePort;
import com.myproject.practico.application.port.out.GoalProgramLinkPersistencePort;
import com.myproject.practico.application.port.out.GoalResolutionStatusPort;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptContentPersistencePort;
import com.myproject.practico.application.port.out.MicroConceptGenerationJobPersistencePort;
import com.myproject.practico.application.port.out.QuickCheckPort;
import com.myproject.practico.application.port.out.QuestionPersistencePort;
import com.myproject.practico.application.port.out.ProgramTreeReadPort;
import com.myproject.practico.application.port.out.ProgramStructurePersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.application.port.out.RuntimeContextStore;
import com.myproject.practico.application.port.out.UserConceptProgressPersistencePort;
import com.myproject.practico.application.port.out.LearningProfilePersistencePort;
import com.myproject.practico.application.service.EvaluationService;
import com.myproject.practico.application.service.GenerateMicroConceptContentService;
import com.myproject.practico.application.service.DefaultLearningEngine;
import com.myproject.practico.application.service.DefaultRetryMasteryPolicy;
import com.myproject.practico.application.service.GetQuestionService;
import com.myproject.practico.application.service.GetLearningStateService;
import com.myproject.practico.application.service.GetCurrentProgramService;
import com.myproject.practico.application.service.GetMicroConceptGenerationStatusService;
import com.myproject.practico.application.service.GenerationMetricsQueryService;
import com.myproject.practico.application.service.GetProgramQueryService;
import com.myproject.practico.application.service.GoalService;
import com.myproject.practico.application.service.GetGoalProgramService;
import com.myproject.practico.application.service.AttachProgramToGoalService;
import com.myproject.practico.application.service.ProgramResolverService;
import com.myproject.practico.application.service.LearningEngine;
import com.myproject.practico.application.service.LearningSessionService;
import com.myproject.practico.application.service.RetryMasteryPolicy;
import com.myproject.practico.application.service.StartLearningService;
import com.myproject.practico.application.service.StartLearningFromGoalService;
import com.myproject.practico.application.service.SubmitAnswerService;
import com.myproject.practico.application.service.ContinueLearningService;
import com.myproject.practico.application.service.LearningSessionStore;
import com.myproject.practico.application.service.QuickCheckService;
import com.myproject.practico.application.service.PracticeService;
import com.myproject.practico.application.service.ProgramQuestionGenerationService;
import com.myproject.practico.application.service.SubmitPracticeService;
import com.myproject.practico.application.service.SubmitQuickCheckService;
import com.myproject.practico.application.service.SubmitRetryService;
import com.myproject.practico.application.service.UserConceptProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class ApplicationWiringConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Executor microConceptGenerationExecutor() {
        return Executors.newFixedThreadPool(2);
    }

    @Bean
    public GoalService goalService(
            GoalPersistencePort goalPersistencePort,
            GoalResolutionStatusPort goalResolutionStatusPort,
            ProgramResolverUseCase programResolverUseCase,
            AttachProgramToGoalUseCase attachProgramToGoalUseCase
    ) {
        return new GoalService(
                goalPersistencePort,
                goalResolutionStatusPort,
                programResolverUseCase,
                attachProgramToGoalUseCase
        );
    }

    @Bean
    public CreateGoalUseCase createGoalUseCase(GoalService goalService) {
        return goalService;
    }

    @Bean
    public ListGoalsUseCase listGoalsUseCase(GoalService goalService) {
        return goalService;
    }

    @Bean
    public GetGoalUseCase getGoalUseCase(GoalService goalService) {
        return goalService;
    }

    @Bean
    public GetGoalResolutionStatusUseCase getGoalResolutionStatusUseCase(GoalService goalService) {
        return goalService;
    }

    @Bean
    public ProgramResolverUseCase programResolverUseCase(
            LearningProgramPersistencePort learningProgramPersistencePort,
            AiCourseGeneratorPort aiCourseGeneratorPort,
            ProgramStructurePersistencePort programStructurePersistencePort,
            ProgramQuestionGenerationService programQuestionGenerationService,
            GenerationMetricsPort generationMetricsPort
    ) {
        return new ProgramResolverService(
                learningProgramPersistencePort,
                aiCourseGeneratorPort,
                programStructurePersistencePort,
                programQuestionGenerationService,
                generationMetricsPort
        );
    }

    @Bean
    public ProgramQuestionGenerationService programQuestionGenerationService(
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            AiQuestionGeneratorPort aiQuestionGeneratorPort,
            GeneratedQuestionPersistencePort generatedQuestionPersistencePort,
            GenerationMetricsPort generationMetricsPort
    ) {
        return new ProgramQuestionGenerationService(
                programMicroConceptReadPort,
                aiQuestionGeneratorPort,
                generatedQuestionPersistencePort,
                generationMetricsPort
        );
    }

    @Bean
    public AttachProgramToGoalUseCase attachProgramToGoalUseCase(
            GoalProgramLinkPersistencePort goalProgramLinkPersistencePort
    ) {
        return new AttachProgramToGoalService(goalProgramLinkPersistencePort);
    }

    @Bean
    public StartLearningFromGoalUseCase startLearningFromGoalUseCase(
            GoalPersistencePort goalPersistencePort,
            GoalProgramLinkPersistencePort goalProgramLinkPersistencePort,
            ProgramResolverUseCase programResolverUseCase,
            AttachProgramToGoalUseCase attachProgramToGoalUseCase,
            StartLearningUseCase startLearningUseCase,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            RuntimeContextStore runtimeContextStore
    ) {
        return new StartLearningFromGoalService(
                goalPersistencePort,
                goalProgramLinkPersistencePort,
                programResolverUseCase,
                attachProgramToGoalUseCase,
                startLearningUseCase,
                programMicroConceptReadPort,
                microConceptContentPersistencePort,
                runtimeContextStore
        );
    }

    @Bean
    public GetGoalProgramUseCase getGoalProgramUseCase(
            GoalPersistencePort goalPersistencePort,
            GoalProgramLinkPersistencePort goalProgramLinkPersistencePort,
            LearningProgramPersistencePort learningProgramPersistencePort
    ) {
        return new GetGoalProgramService(
                goalPersistencePort,
                goalProgramLinkPersistencePort,
                learningProgramPersistencePort
        );
    }

    @Bean
    public GetQuestionUseCase getQuestionUseCase(QuestionPersistencePort questionPersistencePort) {
        return new GetQuestionService(questionPersistencePort);
    }

    @Bean
    public GetCurrentProgramUseCase getCurrentProgramUseCase(
            QuestionPersistencePort questionPersistencePort,
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase,
            RuntimeContextStore runtimeContextStore,
            GoalPersistencePort goalPersistencePort,
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramTreeReadPort programTreeReadPort
    ) {
        return new GetCurrentProgramService(
                questionPersistencePort,
                learningSessionService,
                getQuestionUseCase,
                runtimeContextStore,
                goalPersistencePort,
                learningProgramPersistencePort,
                programTreeReadPort
        );
    }

    @Bean
    public GetProgramQueryService getProgramQueryService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramTreeReadPort programTreeReadPort
    ) {
        return new GetProgramQueryService(learningProgramPersistencePort, programTreeReadPort);
    }

    @Bean
    public GetProgramByIdUseCase getProgramByIdUseCase(GetProgramQueryService getProgramQueryService) {
        return getProgramQueryService;
    }

    @Bean
    public GetProgramTreeUseCase getProgramTreeUseCase(GetProgramQueryService getProgramQueryService) {
        return getProgramQueryService;
    }

    @Bean
    public GetProgramStatusUseCase getProgramStatusUseCase(GetProgramQueryService getProgramQueryService) {
        return getProgramQueryService;
    }

    @Bean
    public GenerationMetricsQueryService generationMetricsQueryService(
            GenerationMetricsPort generationMetricsPort
    ) {
        return new GenerationMetricsQueryService(generationMetricsPort);
    }

    @Bean
    public GetGenerationMetricsUseCase getGenerationMetricsUseCase(
            GenerationMetricsQueryService generationMetricsQueryService
    ) {
        return generationMetricsQueryService;
    }

    @Bean
    public LearningStateAssembler learningStateAssembler(GetQuestionUseCase getQuestionUseCase) {
        return new LearningStateAssembler(getQuestionUseCase);
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
    public GetLearningStateUseCase getLearningStateUseCase(
            LearningSessionService learningSessionService,
            LearningStateAssembler learningStateAssembler
    ) {
        return new GetLearningStateService(learningSessionService, learningStateAssembler);
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
    public PracticeService practiceService() {
        return new PracticeService();
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
            LearningSessionService learningSessionService,
            LearningStateAssembler learningStateAssembler
    ) {
        return new StartLearningService(getQuestionUseCase, learningSessionService, learningStateAssembler);
    }

    @Bean
    public SubmitAnswerUseCase submitAnswerUseCase(
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase,
            LearningEngine learningEngine,
            LearningProfilePersistencePort learningProfilePersistencePort,
            AnswerPersistencePort answerPersistencePort,
            LearningStateAssembler learningStateAssembler,
            RuntimeContextStore runtimeContextStore,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            com.fasterxml.jackson.databind.ObjectMapper objectMapper
    ) {
        return new SubmitAnswerService(
                learningSessionService,
                getQuestionUseCase,
                learningEngine,
                learningProfilePersistencePort,
                answerPersistencePort,
                learningStateAssembler,
                runtimeContextStore,
                microConceptContentPersistencePort,
                objectMapper
        );
    }

    @Bean
    public ContinueLearningUseCase continueLearningUseCase(
            LearningSessionService learningSessionService,
            LearningStateAssembler learningStateAssembler
    ) {
        return new ContinueLearningService(learningSessionService, learningStateAssembler);
    }

    @Bean
    public SubmitPracticeUseCase submitPracticeUseCase(
            LearningSessionService learningSessionService,
            PracticeService practiceService,
            GetQuestionUseCase getQuestionUseCase,
            LearningStateAssembler learningStateAssembler
    ) {
        return new SubmitPracticeService(
                learningSessionService,
                practiceService,
                getQuestionUseCase,
                learningStateAssembler
        );
    }

    @Bean
    public SubmitQuickCheckUseCase submitQuickCheckUseCase(
            LearningSessionService learningSessionService,
            QuickCheckService quickCheckService,
            LearningStateAssembler learningStateAssembler
    ) {
        return new SubmitQuickCheckService(learningSessionService, quickCheckService, learningStateAssembler);
    }

    @Bean
    public SubmitRetryUseCase submitRetryUseCase(
            SubmitAnswerUseCase submitAnswerUseCase
    ) {
        return new SubmitRetryService(submitAnswerUseCase);
    }

    @Bean
    public GenerateMicroConceptContentUseCase generateMicroConceptContentUseCase(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort,
            MicroConceptGenerationJobPersistencePort microConceptGenerationJobPersistencePort,
            QuestionPersistencePort questionPersistencePort,
            ObjectMapper objectMapper,
            Executor microConceptGenerationExecutor
    ) {
        return new GenerateMicroConceptContentService(
                learningProgramPersistencePort,
                programMicroConceptReadPort,
                microConceptContentPersistencePort,
                microConceptGenerationJobPersistencePort,
                questionPersistencePort,
                objectMapper,
                microConceptGenerationExecutor
        );
    }

    @Bean
    public GetMicroConceptGenerationStatusUseCase getMicroConceptGenerationStatusUseCase(
            LearningProgramPersistencePort learningProgramPersistencePort,
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            MicroConceptGenerationJobPersistencePort microConceptGenerationJobPersistencePort,
            MicroConceptContentPersistencePort microConceptContentPersistencePort
    ) {
        return new GetMicroConceptGenerationStatusService(
                learningProgramPersistencePort,
                programMicroConceptReadPort,
                microConceptGenerationJobPersistencePort,
                microConceptContentPersistencePort
        );
    }

}
