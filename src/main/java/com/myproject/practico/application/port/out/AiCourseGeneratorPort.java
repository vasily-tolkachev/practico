package com.myproject.practico.application.port.out;

import com.myproject.practico.application.program.GeneratedProgramStructureResult;

public interface AiCourseGeneratorPort {

    GeneratedProgramStructureResult generateProgramStructure(String goalTitle, String goalDescription);
}
