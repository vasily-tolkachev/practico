package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.LearningProgram;
import com.myproject.practico.domain.LearningProgramOrigin;
import com.myproject.practico.domain.LearningProgramStatus;

import java.util.Optional;

public interface LearningProgramPersistencePort {

    LearningProgram create(
            String title,
            String description,
            LearningProgramStatus status,
            LearningProgramOrigin origin
    );

    Optional<LearningProgram> findById(Long id);

    Optional<LearningProgram> updateStatus(Long id, LearningProgramStatus status);
}
