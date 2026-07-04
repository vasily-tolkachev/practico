package com.myproject.practico.application.port.out;

import com.myproject.practico.application.program.GeneratedQuestionBatch;

public interface AiQuestionGeneratorPort {

    GeneratedQuestionBatch generateQuestions(
            String goalTitle,
            String topicName,
            String conceptName,
            String microConceptName
    );
}
