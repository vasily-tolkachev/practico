package com.myproject.practico.application.port.in;

import com.myproject.practico.application.program.LearningProgram;

public interface GetCurrentProgramUseCase {
    LearningProgram getCurrentProgram(String userId);
}
