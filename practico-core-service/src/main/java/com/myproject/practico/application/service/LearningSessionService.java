package com.myproject.practico.application.service;

import com.myproject.practico.domain.Difficulty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class LearningSessionService {
    private static final int ANSWERS_FOR_MEDIUM = 2;
    private static final int ANSWERS_FOR_HARD = 3;
    private static final double PROMOTION_AVERAGE_THRESHOLD = 8.0;
    private static final int EASY_INDEX = 0;
    private static final int MEDIUM_INDEX = 1;
    private static final int HARD_INDEX = 2;

    private static final List<Difficulty> DIFFICULTY_LEVELS = List.of(
            Difficulty.EASY,
            Difficulty.MEDIUM,
            Difficulty.HARD
    );

    private final LearningSessionStore learningSessionStore;

    public LearningSessionService(LearningSessionStore learningSessionStore) {
        this.learningSessionStore = learningSessionStore;
    }

    public Optional<LearningSessionStore.LearningSession> getSession(String userId) {
        return learningSessionStore.get(userId);
    }

    public void startLearningSession(String userId, Long firstConceptId, Long firstQuestionId) {
        learningSessionStore.startLearningSession(userId, firstConceptId, firstQuestionId);
    }

    public void recordAnswerAndSetNextQuestion(String userId, int score, Long nextQuestionId) {
        learningSessionStore.recordAnswerAndSetNextQuestion(userId, score, nextQuestionId);
    }

    public void setPhase(String userId, LearningPhase phase) {
        learningSessionStore.setPhase(userId, phase);
    }

    public void setCurrentQuestion(String userId, Long conceptId, Long questionId) {
        learningSessionStore.setCurrentQuestion(userId, conceptId, questionId);
    }

    public void setCurrentCycle(String userId, LearningCycle currentCycle) {
        learningSessionStore.setCurrentCycle(userId, currentCycle);
    }

    public void markMicroConceptMastered(String userId, Long microConceptId) {
        learningSessionStore.markMicroConceptMastered(userId, microConceptId);
    }

    public void setPracticeIndex(String userId, int practiceIndex) {
        learningSessionStore.setPracticeIndex(userId, practiceIndex);
    }

    public Difficulty firstDifficulty() {
        return Difficulty.EASY;
    }

    public Difficulty nextDifficulty(LearningSessionStore.LearningSession session, int latestScore) {
        int answeredAfterCurrent = session.answeredCount() + 1;
        int baseLevel = answeredAfterCurrent >= ANSWERS_FOR_HARD
                ? HARD_INDEX
                : answeredAfterCurrent >= ANSWERS_FOR_MEDIUM
                ? MEDIUM_INDEX
                : EASY_INDEX;

        double average = averageWithLatest(session, latestScore);
        if (average > PROMOTION_AVERAGE_THRESHOLD) {
            baseLevel = Math.min(HARD_INDEX, baseLevel + 1);
        }

        return DIFFICULTY_LEVELS.get(baseLevel);
    }

    public Set<Long> excludedQuestionIds(LearningSessionStore.LearningSession session) {
        Set<Long> excluded = new HashSet<>(session.answeredQuestionIds());
        if (session.currentQuestionId() != null) {
            excluded.add(session.currentQuestionId());
        }
        return excluded;
    }

    public double averageLastScores(LearningSessionStore.LearningSession session) {
        if (session.lastScores().isEmpty()) {
            return 0.0;
        }
        return session.lastScores().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    private double averageWithLatest(LearningSessionStore.LearningSession session, int latestScore) {
        List<Integer> scores = new ArrayList<>(session.lastScores());
        scores.add(latestScore);
        if (scores.size() > ANSWERS_FOR_HARD) {
            scores.remove(0);
        }
        return scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
}
