package com.myproject.practico.application.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SessionService {

    private static final List<String> DIFFICULTY_LEVELS = List.of("easy", "medium", "hard");

    private final UserSessionStore userSessionStore;

    public SessionService(UserSessionStore userSessionStore) {
        this.userSessionStore = userSessionStore;
    }

    public Optional<UserSessionStore.UserSession> getSession(String userId) {
        return userSessionStore.get(userId);
    }

    public void startSession(String userId, Long firstQuestionId) {
        userSessionStore.startSession(userId, firstQuestionId);
    }

    public void recordAnswerAndSetNextQuestion(String userId, int score, Long nextQuestionId) {
        userSessionStore.recordAnswerAndSetNextQuestion(userId, score, nextQuestionId);
    }

    public String firstDifficulty() {
        return "easy";
    }

    public String nextDifficulty(UserSessionStore.UserSession session, int latestScore) {
        int answeredAfterCurrent = session.answeredCount() + 1;
        int baseLevel = answeredAfterCurrent >= 3 ? 2 : answeredAfterCurrent >= 2 ? 1 : 0;

        double average = averageWithLatest(session, latestScore);
        if (average > 8.0) {
            baseLevel = Math.min(2, baseLevel + 1);
        }

        return DIFFICULTY_LEVELS.get(baseLevel);
    }

    public Set<Long> excludedQuestionIds(UserSessionStore.UserSession session) {
        Set<Long> excluded = new HashSet<>(session.answeredQuestionIds());
        if (session.currentQuestionId() != null) {
            excluded.add(session.currentQuestionId());
        }
        return excluded;
    }

    public double averageLastScores(UserSessionStore.UserSession session) {
        if (session.lastScores().isEmpty()) {
            return 0.0;
        }
        return session.lastScores().stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    private double averageWithLatest(UserSessionStore.UserSession session, int latestScore) {
        List<Integer> scores = new ArrayList<>(session.lastScores());
        scores.add(latestScore);
        if (scores.size() > 3) {
            scores.remove(0);
        }
        return scores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }
}
