package com.myproject.practico.application.port.out;

import com.myproject.practico.application.program.ProgramMicroConceptTarget;

import java.util.List;

public interface ProgramMicroConceptReadPort {

    List<ProgramMicroConceptTarget> findByProgramId(Long programId);

    boolean hasQuestions(Long microConceptId);
}
