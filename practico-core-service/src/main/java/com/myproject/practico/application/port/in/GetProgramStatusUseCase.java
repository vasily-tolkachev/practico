package com.myproject.practico.application.port.in;

import com.myproject.practico.application.program.ProgramGenerationStatus;

import java.util.Optional;

public interface GetProgramStatusUseCase {

    Optional<ProgramGenerationStatus> getStatus(Long programId);
}
