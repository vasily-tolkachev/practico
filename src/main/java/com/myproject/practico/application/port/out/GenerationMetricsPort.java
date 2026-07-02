package com.myproject.practico.application.port.out;

import com.myproject.practico.application.program.GenerationStage;
import com.myproject.practico.application.program.GenerationStageMetrics;

import java.util.List;

public interface GenerationMetricsPort {

    void recordSuccess(GenerationStage stage, long latencyMs, Long tokens, Double estimatedCostUsd);

    void recordFailure(GenerationStage stage, long latencyMs, Long tokens, Double estimatedCostUsd);

    List<GenerationStageMetrics> snapshot();
}
