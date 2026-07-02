package com.myproject.practico.application.port.out;

import com.myproject.practico.application.program.GeneratedProgramStructure;

public interface ProgramStructurePersistencePort {

    void persist(Long programId, GeneratedProgramStructure structure);
}
