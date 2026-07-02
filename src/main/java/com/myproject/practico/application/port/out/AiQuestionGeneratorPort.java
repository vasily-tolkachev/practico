package com.myproject.practico.application.port.out;

import com.myproject.practico.application.program.GeneratedQuestion;

import java.util.List;

public interface AiQuestionGeneratorPort {

    List<GeneratedQuestion> generateQuestions(
            String goalTitle,
            String topicName,
            String conceptName,
            String microConceptName
    );
}
