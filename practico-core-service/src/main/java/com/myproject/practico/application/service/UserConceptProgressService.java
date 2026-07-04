package com.myproject.practico.application.service;

import com.myproject.practico.application.port.out.UserConceptProgressPersistencePort;
import com.myproject.practico.domain.ProgressStatus;
import com.myproject.practico.domain.UserConceptProgress;

import java.time.Instant;

public class UserConceptProgressService {
    private static final int MASTERY_THRESHOLD = 8;
    private static final int IN_PROGRESS_MIN = 5;

    private final UserConceptProgressPersistencePort userConceptProgressPersistencePort;

    public UserConceptProgressService(UserConceptProgressPersistencePort userConceptProgressPersistencePort) {
        this.userConceptProgressPersistencePort = userConceptProgressPersistencePort;
    }

    public UserConceptProgress update(Long userId, Long conceptId, int score, Instant updatedAt) {
        return recordAttempt(userId, conceptId, score, updatedAt);
    }

    public UserConceptProgress recordAttempt(Long userId, Long conceptId, int score, Instant updatedAt) {
        UserConceptProgress existing = userConceptProgressPersistencePort
                .findByUserIdAndConceptId(userId, conceptId)
                .orElse(null);

        int totalAnswers = existing == null ? 1 : existing.totalAnswers() + 1;
        int correctAnswers = existing == null
                ? (isCorrect(score) ? 1 : 0)
                : existing.correctAnswers() + (isCorrect(score) ? 1 : 0);

        ProgressStatus status = resolveStatus(score);

        return userConceptProgressPersistencePort.upsert(
                userId,
                conceptId,
                status,
                correctAnswers,
                totalAnswers,
                updatedAt
        );
    }

    public UserConceptProgress markMastered(Long userId, Long conceptId, Instant updatedAt) {
        UserConceptProgress existing = userConceptProgressPersistencePort
                .findByUserIdAndConceptId(userId, conceptId)
                .orElse(null);

        int totalAnswers = existing == null ? 0 : existing.totalAnswers();
        int correctAnswers = existing == null ? 0 : existing.correctAnswers();

        return userConceptProgressPersistencePort.upsert(
                userId,
                conceptId,
                ProgressStatus.MASTERED,
                correctAnswers,
                totalAnswers,
                updatedAt
        );
    }

    private boolean isCorrect(int score) {
        return score >= MASTERY_THRESHOLD;
    }

    private ProgressStatus resolveStatus(int score) {
        if (score < IN_PROGRESS_MIN) {
            return ProgressStatus.LEARNING;
        }
        return ProgressStatus.IN_PROGRESS;
    }
}
