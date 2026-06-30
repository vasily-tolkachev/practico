package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.QuickCheck;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContinueLearningServiceTest {

    @Test
    void shouldMoveToPracticeWhenPracticeExists() {
        LearningSessionService sessionService = sessionServiceWithLearningCardCycle(new LearningCycle(
                null,
                null,
                List.of(new PracticeItem(PracticeType.TRUE_FALSE, "q", List.of(), List.of(), true, false)),
                List.of(),
                "retry"
        ));
        ContinueLearningService service = new ContinueLearningService(sessionService, new LearningStateAssembler(new FakeQuestions()));

        LearningState state = service.continueLearning("u1");
        assertEquals(LearningPhase.PRACTICE, state.phase());
    }

    @Test
    void shouldMoveToQuickCheckWhenNoPracticeButQuickCheckExists() {
        LearningSessionService sessionService = sessionServiceWithLearningCardCycle(new LearningCycle(
                null,
                new QuickCheck("qc?", "yes"),
                List.of(),
                List.of(),
                "retry"
        ));
        ContinueLearningService service = new ContinueLearningService(sessionService, new LearningStateAssembler(new FakeQuestions()));

        LearningState state = service.continueLearning("u1");
        assertEquals(LearningPhase.QUICK_CHECK, state.phase());
    }

    @Test
    void shouldMoveToRetryWhenNoPracticeAndNoQuickCheck() {
        LearningSessionService sessionService = sessionServiceWithLearningCardCycle(new LearningCycle(
                null,
                null,
                List.of(),
                List.of(),
                "retry"
        ));
        ContinueLearningService service = new ContinueLearningService(sessionService, new LearningStateAssembler(new FakeQuestions()));

        LearningState state = service.continueLearning("u1");
        assertEquals(LearningPhase.RETRY, state.phase());
    }

    private LearningSessionService sessionServiceWithLearningCardCycle(LearningCycle cycle) {
        LearningSessionStore store = new LearningSessionStore();
        LearningSessionService sessionService = new LearningSessionService(store);
        store.startLearningSession("u1", 1L, 1L);
        store.setPhase("u1", LearningPhase.LEARNING_CARD);
        store.setCurrentCycle("u1", cycle);
        return sessionService;
    }

    private static class FakeQuestions implements GetQuestionUseCase {
        @Override public Optional<Question> getNext(Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextInConcept(Long conceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextInMicroConcept(Long conceptId, Long microConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextFromNextMicroConcept(Long conceptId, Long currentMicroConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextFromNextConcept(Long currentConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getFirstFromTopic(String topicName, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getFirstFromConceptName(String conceptName, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getById(Long id) { return Optional.empty(); }
        @Override public int conceptOrder(Long conceptId) { return 1; }
        @Override public int totalConcepts() { return 6; }
        @Override public int microConceptOrder(Long conceptId, Long microConceptId) { return 1; }
        @Override public int totalMicroConcepts(Long conceptId) { return 6; }
    }
}
