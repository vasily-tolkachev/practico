package com.myproject.practico.application.port.out;

import com.myproject.practico.application.program.GeneratedProgramStructure;

public interface AiCourseGeneratorPort {

    GeneratedProgramStructure generateProgramStructure(String goalTitle, String goalDescription);
}
