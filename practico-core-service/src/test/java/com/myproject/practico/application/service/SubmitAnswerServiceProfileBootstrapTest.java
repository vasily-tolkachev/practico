package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.out.AnswerPersistencePort;
import com.myproject.practico.application.port.out.LearningProfilePersistencePort;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.LearningProfile;
import com.myproject.practico.domain.Question;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SubmitAnswerServiceProfileBootstrapTest {

    @Test
    void shouldEnsureLearningProfileExistsForAuthenticatedUser() {
        AtomicInteger ensureCalls = new AtomicInteger();
        AtomicInteger touchCalls = new AtomicInteger();

        LearningProfilePersistencePort profilePort = new LearningProfilePersistencePort() {
            @Override
            public Optional<LearningProfile> findById(UUID userId) {
                return Optional.empty();
            }

            @Override
            public LearningProfile ensureExists(UUID userId, Instant now) {
                ensureCalls.incrementAndGet();
                return new LearningProfile(userId, "Learner", now, now);
            }

            @Override
            public LearningProfile touch(UUID userId, Instant now) {
                touchCalls.incrementAndGet();
                return new LearningProfile(userId, "Learner", now, now);
            }
        };

        AnswerPersistencePort answerPort = answer -> {};
        LearningSessionStore store = new LearningSessionStore();
        LearningSessionService sessionService = new LearningSessionService(store);
        String userId = UUID.randomUUID().toString();
        store.startLearningSession(userId, 1L, 1L);

        GetQuestionUseCase questions = new FakeQuestions();
        LearningEngine engine = new StubLearningEngine();
        LearningStateAssembler assembler = new LearningStateAssembler(questions);

        SubmitAnswerService service = new SubmitAnswerService(
                sessionService,
                questions,
                engine,
                profilePort,
                answerPort,
                assembler
        );

        LearningState state = service.submit(userId, "any");
        assertNotNull(state);
        assertEquals(1, ensureCalls.get());
        assertEquals(1, touchCalls.get());
    }

    private static class StubLearningEngine implements LearningEngine {
        @Override
        public LearningResult handleQuestionAnswer(UUID userId, Question currentQuestion, String answer, LearningSessionStore.LearningSession session, Instant now) {
            return new LearningResult(
                    new EvaluationResult(7, true, "ok", null, null, java.util.List.of(), java.util.List.of(), null),
                    null,
                    LearningPhase.COMPLETED,
                    null
            );
        }

        @Override
        public LearningResult handleRetryAnswer(UUID userId, Question currentQuestion, String answer, LearningSessionStore.LearningSession session, Instant now) {
            return handleQuestionAnswer(userId, currentQuestion, answer, session, now);
        }
    }

    private static class FakeQuestions implements GetQuestionUseCase {
        @Override public Optional<Question> getNext(Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextInConcept(Long conceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextInMicroConcept(Long conceptId, Long microConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextFromNextMicroConcept(Long conceptId, Long currentMicroConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextFromNextConcept(Long currentConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getFirstFromTopic(String topicName, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getFirstFromConceptName(String conceptName, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getById(Long id) {
            return Optional.of(new Question(
                    id,
                    "q",
                    null,
                    Difficulty.EASY,
                    null,
                    com.myproject.practico.domain.QuestionType.DEFINITION,
                    "a",
                    "e"
            ));
        }
        @Override public int conceptOrder(Long conceptId) { return 1; }
        @Override public int totalConcepts() { return 1; }
        @Override public int microConceptOrder(Long conceptId, Long microConceptId) { return 1; }
        @Override public int totalMicroConcepts(Long conceptId) { return 1; }
    }
}
