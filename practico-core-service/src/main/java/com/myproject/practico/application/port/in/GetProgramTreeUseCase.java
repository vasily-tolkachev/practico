package com.myproject.practico.application.port.in;

import com.myproject.practico.application.program.LearningProgram;

import java.util.Optional;

public interface GetProgramTreeUseCase {

    Optional<LearningProgram> getTree(Long programId);
}
