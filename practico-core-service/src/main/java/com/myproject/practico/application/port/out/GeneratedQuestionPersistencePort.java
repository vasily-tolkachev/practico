package com.myproject.practico.application.port.out;

import com.myproject.practico.application.program.GeneratedQuestion;

import java.util.List;

public interface GeneratedQuestionPersistencePort {

    void save(Long microConceptId, List<GeneratedQuestion> questions);
}
