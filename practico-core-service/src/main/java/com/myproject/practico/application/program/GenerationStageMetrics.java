package com.myproject.practico.application.program;

import java.time.Instant;

public record GenerationStageMetrics(
        GenerationStage stage,
        long attempts,
        long failures,
        double failureRate,
        long averageLatencyMs,
        long lastLatencyMs,
        long totalTokens,
        double totalEstimatedCostUsd,
        Instant updatedAt
) {
}
