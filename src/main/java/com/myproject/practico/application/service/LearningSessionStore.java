package com.myproject.practico.application.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LearningSessionStore {

    private static final int MAX_LAST_SCORES = 3;
    private static final int MAX_ANSWERED_IDS = 100;

    private final Map<String, LearningSession> sessions = new ConcurrentHashMap<>();

    public void startLearningSession(String userId, Long conceptId, Long questionId) {
        sessions.put(userId, LearningSession.started(userId, conceptId, questionId));
    }

    public void recordAnswerAndSetNextQuestion(String userId, int score, Long nextQuestionId) {
        sessions.computeIfPresent(userId, (id, session) -> session.recordAnswer(score, nextQuestionId));
    }

    public void setPhase(String userId, LearningPhase phase) {
        sessions.computeIfPresent(userId, (id, session) -> session.withPhase(phase));
    }

    public void setCurrentQuestion(String userId, Long conceptId, Long questionId) {
        sessions.computeIfPresent(userId, (id, session) -> session.withCurrentQuestion(conceptId, questionId));
    }

    public void setCurrentCycle(String userId, LearningCycle currentCycle) {
        sessions.computeIfPresent(userId, (id, session) -> session.withCurrentCycle(currentCycle));
    }

    public Optional<LearningSession> get(String userId) {
        return Optional.ofNullable(sessions.get(userId));
    }

    public record LearningSession(
            String userId,
            Long currentConceptId,
            Long currentQuestionId,
            LearningPhase phase,
            LearningCycle currentCycle,
            Deque<Integer> lastScores,
            Deque<Long> answeredQuestionIds,
            int answeredCount
    ) {
        public LearningSession {
            lastScores = new ArrayDeque<>(lastScores);
            answeredQuestionIds = new ArrayDeque<>(answeredQuestionIds);
        }

        public static LearningSession started(String userId, Long conceptId, Long questionId) {
            return new LearningSession(
                    userId,
                    conceptId,
                    questionId,
                    LearningPhase.QUESTION,
                    null,
                    new ArrayDeque<>(),
                    new ArrayDeque<>(),
                    0
            );
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

        public LearningSession recordAnswer(int score, Long nextQuestionId) {
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

            return new LearningSession(
                    this.userId,
                    this.currentConceptId,
                    nextQuestionId,
                    this.phase,
                    this.currentCycle,
                    updatedScores,
                    updatedAnsweredQuestionIds,
                    this.answeredCount + 1
            );
        }

        public LearningSession withPhase(LearningPhase phase) {
            return new LearningSession(
                    this.userId,
                    this.currentConceptId,
                    this.currentQuestionId,
                    phase,
                    this.currentCycle,
                    this.lastScores,
                    this.answeredQuestionIds,
                    this.answeredCount
            );
        }

        public LearningSession withCurrentQuestion(Long conceptId, Long questionId) {
            return new LearningSession(
                    this.userId,
                    conceptId,
                    questionId,
                    this.phase,
                    this.currentCycle,
                    this.lastScores,
                    this.answeredQuestionIds,
                    this.answeredCount
            );
        }

        public LearningSession withCurrentCycle(LearningCycle currentCycle) {
            return new LearningSession(
                    this.userId,
                    this.currentConceptId,
                    this.currentQuestionId,
                    this.phase,
                    currentCycle,
                    this.lastScores,
                    this.answeredQuestionIds,
                    this.answeredCount
            );
        }
    }
}
