package com.myproject.practico.application.port.in;

import com.myproject.practico.application.program.GenerationStageMetrics;

import java.util.List;

public interface GetGenerationMetricsUseCase {

    List<GenerationStageMetrics> getMetrics();
}
