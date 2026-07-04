package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.application.port.out.GenerationMetricsPort;
import com.myproject.practico.application.program.GenerationStage;
import com.myproject.practico.application.program.GenerationStageMetrics;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;

@Repository
public class InMemoryGenerationMetricsAdapter implements GenerationMetricsPort {

    private final Map<GenerationStage, StageStats> statsByStage = new ConcurrentHashMap<>();

    @Override
    public void recordSuccess(GenerationStage stage, long latencyMs, Long tokens, Double estimatedCostUsd) {
        StageStats stats = statsByStage.computeIfAbsent(stage, ignored -> new StageStats());
        stats.attempts.incrementAndGet();
        stats.totalLatencyMs.addAndGet(Math.max(0L, latencyMs));
        stats.lastLatencyMs.set(Math.max(0L, latencyMs));
        if (tokens != null && tokens > 0) {
            stats.totalTokens.addAndGet(tokens);
        }
        if (estimatedCostUsd != null && estimatedCostUsd > 0) {
            stats.totalEstimatedCostUsd.add(estimatedCostUsd);
        }
        stats.updatedAtEpochMs.set(System.currentTimeMillis());
    }

    @Override
    public void recordFailure(GenerationStage stage, long latencyMs, Long tokens, Double estimatedCostUsd) {
        StageStats stats = statsByStage.computeIfAbsent(stage, ignored -> new StageStats());
        stats.failures.incrementAndGet();
        recordSuccess(stage, latencyMs, tokens, estimatedCostUsd);
    }

    @Override
    public List<GenerationStageMetrics> snapshot() {
        return Arrays.stream(GenerationStage.values())
                .map(stage -> {
                    StageStats stats = statsByStage.computeIfAbsent(stage, ignored -> new StageStats());
                    long attempts = stats.attempts.get();
                    long failures = stats.failures.get();
                    double failureRate = attempts <= 0 ? 0.0 : (double) failures / (double) attempts;
                    long avgLatency = attempts <= 0 ? 0 : stats.totalLatencyMs.get() / attempts;
                    long updatedAtMs = stats.updatedAtEpochMs.get();
                    return new GenerationStageMetrics(
                            stage,
                            attempts,
                            failures,
                            failureRate,
                            avgLatency,
                            stats.lastLatencyMs.get(),
                            stats.totalTokens.get(),
                            stats.totalEstimatedCostUsd.sum(),
                            updatedAtMs <= 0 ? Instant.EPOCH : Instant.ofEpochMilli(updatedAtMs)
                    );
                })
                .toList();
    }

    private static final class StageStats {
        private final AtomicLong attempts = new AtomicLong();
        private final AtomicLong failures = new AtomicLong();
        private final AtomicLong totalLatencyMs = new AtomicLong();
        private final AtomicLong lastLatencyMs = new AtomicLong();
        private final AtomicLong totalTokens = new AtomicLong();
        private final DoubleAdder totalEstimatedCostUsd = new DoubleAdder();
        private final AtomicLong updatedAtEpochMs = new AtomicLong();
    }
}
