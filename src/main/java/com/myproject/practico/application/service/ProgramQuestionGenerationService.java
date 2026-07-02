package com.myproject.practico.application.service;

import com.myproject.practico.application.port.out.AiQuestionGeneratorPort;
import com.myproject.practico.application.port.out.GeneratedQuestionPersistencePort;
import com.myproject.practico.application.port.out.ProgramMicroConceptReadPort;
import com.myproject.practico.application.program.GeneratedQuestion;
import com.myproject.practico.application.program.ProgramMicroConceptTarget;

import java.util.List;

public class ProgramQuestionGenerationService {

    private final ProgramMicroConceptReadPort programMicroConceptReadPort;
    private final AiQuestionGeneratorPort aiQuestionGeneratorPort;
    private final GeneratedQuestionPersistencePort generatedQuestionPersistencePort;

    public ProgramQuestionGenerationService(
            ProgramMicroConceptReadPort programMicroConceptReadPort,
            AiQuestionGeneratorPort aiQuestionGeneratorPort,
            GeneratedQuestionPersistencePort generatedQuestionPersistencePort
    ) {
        this.programMicroConceptReadPort = programMicroConceptReadPort;
        this.aiQuestionGeneratorPort = aiQuestionGeneratorPort;
        this.generatedQuestionPersistencePort = generatedQuestionPersistencePort;
    }

    public void generateForProgram(Long programId, String goalTitle) {
        List<ProgramMicroConceptTarget> targets = programMicroConceptReadPort.findByProgramId(programId);
        for (ProgramMicroConceptTarget target : targets) {
            if (target.microConceptId() == null || programMicroConceptReadPort.hasQuestions(target.microConceptId())) {
                continue;
            }
            List<GeneratedQuestion> generated = aiQuestionGeneratorPort.generateQuestions(
                    goalTitle,
                    target.topicName(),
                    target.conceptName(),
                    target.microConceptName()
            );
            if (generated == null || generated.isEmpty()) {
                continue;
            }
            generatedQuestionPersistencePort.save(target.microConceptId(), generated);
        }
    }
}
