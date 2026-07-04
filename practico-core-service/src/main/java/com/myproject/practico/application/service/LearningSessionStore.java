package com.myproject.practico.application.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class LearningSessionStore {
    private static final int MAX_LAST_SCORES = 3;
    private static final int MAX_ANSWERED_IDS = 100;
    private static final int MAX_MASTERED_MICRO_CONCEPT_IDS = 200;

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

    public void setPracticeIndex(String userId, int practiceIndex) {
        sessions.computeIfPresent(userId, (id, session) -> session.withPracticeIndex(practiceIndex));
    }

    public Optional<LearningSession> get(String userId) {
        return Optional.ofNullable(sessions.get(userId));
    }

    public void markMicroConceptMastered(String userId, Long microConceptId) {
        sessions.computeIfPresent(userId, (id, session) -> session.withMasteredMicroConcept(microConceptId));
    }

    public record LearningSession(
            String userId,
            Long currentConceptId,
            Long currentQuestionId,
            LearningPhase phase,
            LearningCycle currentCycle,
            int currentPracticeIndex,
            Deque<Integer> lastScores,
            Deque<Long> answeredQuestionIds,
            Deque<Long> masteredMicroConceptIds,
            int answeredCount
    ) {
        public LearningSession {
            lastScores = new ArrayDeque<>(lastScores);
            answeredQuestionIds = new ArrayDeque<>(answeredQuestionIds);
            masteredMicroConceptIds = new ArrayDeque<>(masteredMicroConceptIds);
        }

        public static LearningSession started(String userId, Long conceptId, Long questionId) {
            return new LearningSession(
                    userId,
                    conceptId,
                    questionId,
                    LearningPhase.QUESTION,
                    null,
                    0,
                    new ArrayDeque<>(),
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

        @Override
        public Deque<Long> masteredMicroConceptIds() {
            return new ArrayDeque<>(masteredMicroConceptIds);
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
                    this.currentPracticeIndex,
                    updatedScores,
                    updatedAnsweredQuestionIds,
                    this.masteredMicroConceptIds,
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
                    this.currentPracticeIndex,
                    this.lastScores,
                    this.answeredQuestionIds,
                    this.masteredMicroConceptIds,
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
                    this.currentPracticeIndex,
                    this.lastScores,
                    this.answeredQuestionIds,
                    this.masteredMicroConceptIds,
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
                    this.currentPracticeIndex,
                    this.lastScores,
                    this.answeredQuestionIds,
                    this.masteredMicroConceptIds,
                    this.answeredCount
            );
        }

        public LearningSession withPracticeIndex(int practiceIndex) {
            return new LearningSession(
                    this.userId,
                    this.currentConceptId,
                    this.currentQuestionId,
                    this.phase,
                    this.currentCycle,
                    Math.max(0, practiceIndex),
                    this.lastScores,
                    this.answeredQuestionIds,
                    this.masteredMicroConceptIds,
                    this.answeredCount
            );
        }

        public LearningSession withMasteredMicroConcept(Long microConceptId) {
            if (microConceptId == null || this.masteredMicroConceptIds.contains(microConceptId)) {
                return this;
            }

            Deque<Long> updatedMasteredMicroConceptIds = new ArrayDeque<>(this.masteredMicroConceptIds);
            updatedMasteredMicroConceptIds.addLast(microConceptId);
            while (updatedMasteredMicroConceptIds.size() > MAX_MASTERED_MICRO_CONCEPT_IDS) {
                updatedMasteredMicroConceptIds.removeFirst();
            }

            return new LearningSession(
                    this.userId,
                    this.currentConceptId,
                    this.currentQuestionId,
                    this.phase,
                    this.currentCycle,
                    this.currentPracticeIndex,
                    this.lastScores,
                    this.answeredQuestionIds,
                    updatedMasteredMicroConceptIds,
                    this.answeredCount
            );
        }
    }
}
