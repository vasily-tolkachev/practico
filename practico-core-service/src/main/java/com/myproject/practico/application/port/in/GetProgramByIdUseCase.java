package com.myproject.practico.application.port.in;

import com.myproject.practico.domain.LearningProgram;

import java.util.Optional;

public interface GetProgramByIdUseCase {

    Optional<LearningProgram> getById(Long programId);
}
