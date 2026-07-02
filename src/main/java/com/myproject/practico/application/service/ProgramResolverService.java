package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.ProgramResolverUseCase;
import com.myproject.practico.application.port.out.AiCourseGeneratorPort;
import com.myproject.practico.application.port.out.LearningProgramPersistencePort;
import com.myproject.practico.application.port.out.ProgramStructurePersistencePort;
import com.myproject.practico.application.program.GeneratedConceptStructure;
import com.myproject.practico.application.program.GeneratedProgramStructure;
import com.myproject.practico.application.program.GeneratedTopicStructure;
import com.myproject.practico.application.program.LearningProgram;
import com.myproject.practico.application.program.ProgramOrigin;
import com.myproject.practico.application.program.ProgramProgress;
import com.myproject.practico.application.program.ProgramResolutionResult;
import com.myproject.practico.domain.Goal;
import com.myproject.practico.domain.LearningProgramOrigin;
import com.myproject.practico.domain.LearningProgramStatus;
import com.myproject.practico.domain.GoalProgramSourceType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProgramResolverService implements ProgramResolverUseCase {

    private final LearningProgramPersistencePort learningProgramPersistencePort;
    private final AiCourseGeneratorPort aiCourseGeneratorPort;
    private final ProgramStructurePersistencePort programStructurePersistencePort;
    private final ProgramQuestionGenerationService programQuestionGenerationService;

    public ProgramResolverService(
            LearningProgramPersistencePort learningProgramPersistencePort,
            AiCourseGeneratorPort aiCourseGeneratorPort,
            ProgramStructurePersistencePort programStructurePersistencePort,
            ProgramQuestionGenerationService programQuestionGenerationService
    ) {
        this.learningProgramPersistencePort = learningProgramPersistencePort;
        this.aiCourseGeneratorPort = aiCourseGeneratorPort;
        this.programStructurePersistencePort = programStructurePersistencePort;
        this.programQuestionGenerationService = programQuestionGenerationService;
    }

    @Override
    public ProgramResolutionResult resolveForGoal(Goal goal, String userId) {
        String goalTitle = goal == null || goal.title() == null || goal.title().isBlank()
                ? "Goal"
                : goal.title().trim();
        com.myproject.practico.domain.LearningProgram persistedProgram = learningProgramPersistencePort.create(
                goalTitle + " Program",
                "Generated from goal: " + goalTitle,
                LearningProgramStatus.CREATED,
                LearningProgramOrigin.GOAL_BASED
        );
        Long programId = persistedProgram.id();

        try {
            learningProgramPersistencePort.updateStatus(programId, LearningProgramStatus.GENERATING);
            GeneratedProgramStructure generated = aiCourseGeneratorPort.generateProgramStructure(
                    goalTitle,
                    goal == null ? "" : goal.description()
            );
            GeneratedProgramStructure validated = validate(generated);
            if (validated.topics().isEmpty()) {
                throw new IllegalStateException("Generated structure is empty");
            }
            programStructurePersistencePort.persist(programId, validated);
            programQuestionGenerationService.generateForProgram(programId, goalTitle);
            learningProgramPersistencePort.updateStatus(programId, LearningProgramStatus.READY);
        } catch (Exception ex) {
            learningProgramPersistencePort.updateStatus(programId, LearningProgramStatus.FAILED);
            throw ex;
        }

        return new ProgramResolutionResult(
                new LearningProgram(
                        String.valueOf(programId),
                        goal == null ? null : goal.id(),
                        ProgramOrigin.GOAL_BASED,
                        persistedProgram.title(),
                        goalTitle,
                        List.of(),
                        new ProgramProgress(0, 0)
                ),
                GoalProgramSourceType.GENERATED
        );
    }

    private GeneratedProgramStructure validate(GeneratedProgramStructure source) {
        if (source == null || source.topics() == null) {
            return new GeneratedProgramStructure(List.of());
        }

        List<GeneratedTopicStructure> topics = new ArrayList<>();
        Set<String> seenTopics = new HashSet<>();
        for (GeneratedTopicStructure topic : source.topics()) {
            String topicName = normalize(topic == null ? null : topic.name());
            if (topicName == null || !seenTopics.add(topicName.toLowerCase())) {
                continue;
            }

            List<GeneratedConceptStructure> concepts = new ArrayList<>();
            Set<String> seenConcepts = new HashSet<>();
            if (topic.concepts() != null) {
                for (GeneratedConceptStructure concept : topic.concepts()) {
                    String conceptName = normalize(concept == null ? null : concept.name());
                    if (conceptName == null || !seenConcepts.add(conceptName.toLowerCase())) {
                        continue;
                    }

                    List<String> microConcepts = new ArrayList<>();
                    Set<String> seenMicro = new HashSet<>();
                    if (concept.microConcepts() != null) {
                        for (String micro : concept.microConcepts()) {
                            String microName = normalize(micro);
                            if (microName != null && seenMicro.add(microName.toLowerCase())) {
                                microConcepts.add(microName);
                            }
                        }
                    }
                    if (!microConcepts.isEmpty()) {
                        concepts.add(new GeneratedConceptStructure(conceptName, microConcepts));
                    }
                }
            }
            if (!concepts.isEmpty()) {
                topics.add(new GeneratedTopicStructure(topicName, concepts));
            }
        }

        return new GeneratedProgramStructure(topics);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
