package com.myproject.practico.application.service;

import com.myproject.practico.application.port.in.GetGenerationMetricsUseCase;
import com.myproject.practico.application.port.out.GenerationMetricsPort;
import com.myproject.practico.application.program.GenerationStageMetrics;

import java.util.List;

public class GenerationMetricsQueryService implements GetGenerationMetricsUseCase {

    private final GenerationMetricsPort generationMetricsPort;

    public GenerationMetricsQueryService(GenerationMetricsPort generationMetricsPort) {
        this.generationMetricsPort = generationMetricsPort;
    }

    @Override
    public List<GenerationStageMetrics> getMetrics() {
        return generationMetricsPort.snapshot();
    }
}
