package com.myproject.practico.application.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class UserSessionStore {

    private static final int MAX_LAST_SCORES = 3;
    private static final int MAX_ANSWERED_IDS = 100;

    private final Map<String, UserSession> sessions = new ConcurrentHashMap<>();

    public void startSession(String userId, Long questionId) {
        sessions.put(userId, UserSession.started(userId, questionId));
    }

    public void recordAnswerAndSetNextQuestion(String userId, int score, Long nextQuestionId) {
        sessions.computeIfPresent(userId, (id, session) -> session.recordAnswer(score, nextQuestionId));
    }

    public Optional<UserSession> get(String userId) {
        return Optional.ofNullable(sessions.get(userId));
    }

    public record UserSession(
            String userId,
            Long currentQuestionId,
            Deque<Integer> lastScores,
            Deque<Long> answeredQuestionIds,
            int answeredCount
    ) {
        public UserSession {
            lastScores = new ArrayDeque<>(lastScores);
            answeredQuestionIds = new ArrayDeque<>(answeredQuestionIds);
        }

        public static UserSession started(String userId, Long questionId) {
            return new UserSession(userId, questionId, new ArrayDeque<>(), new ArrayDeque<>(), 0);
        }

        @Override
        public Deque<Integer> lastScores() {
            return new ArrayDeque<>(lastScores);
        }

        @Override
        public Deque<Long> answeredQuestionIds() {
            return new ArrayDeque<>(answeredQuestionIds);
        }

        public boolean hasAnswered(Long questionId) {
            if (questionId == null) {
                return false;
            }
            return answeredQuestionIds.contains(questionId);
        }

        public UserSession recordAnswer(int score, Long nextQuestionId) {
            Deque<Integer> updatedScores = new ArrayDeque<>(this.lastScores);
            updatedScores.addLast(score);
            while (updatedScores.size() > MAX_LAST_SCORES) {
                updatedScores.removeFirst();
            }

            Deque<Long> updatedAnsweredQuestionIds = new ArrayDeque<>(this.answeredQuestionIds);
            if (this.currentQuestionId != null) {
                updatedAnsweredQuestionIds.addLast(this.currentQuestionId);
            }
            while (updatedAnsweredQuestionIds.size() > MAX_ANSWERED_IDS) {
                updatedAnsweredQuestionIds.removeFirst();
            }

            return new UserSession(
                    this.userId,
                    nextQuestionId,
                    updatedScores,
                    updatedAnsweredQuestionIds,
                    this.answeredCount + 1
            );
        }
    }
}
