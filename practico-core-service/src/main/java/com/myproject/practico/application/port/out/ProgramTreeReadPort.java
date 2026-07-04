package com.myproject.practico.application.port.out;

import com.myproject.practico.application.program.ProgramConcept;

import java.util.List;

public interface ProgramTreeReadPort {

    List<ProgramConcept> findConceptTreeByProgramId(Long programId);
}
