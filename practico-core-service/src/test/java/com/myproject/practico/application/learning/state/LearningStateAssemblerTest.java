package com.myproject.practico.application.learning.state;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.service.LearningCycle;
import com.myproject.practico.application.service.LearningPhase;
import com.myproject.practico.application.service.LearningSessionStore;
import com.myproject.practico.application.service.PracticeItem;
import com.myproject.practico.application.service.PracticeType;
import com.myproject.practico.domain.Concept;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.Question;
import com.myproject.practico.domain.QuestionType;
import com.myproject.practico.domain.Topic;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class LearningStateAssemblerTest {

    @Test
    void shouldBuildQuestionActivityState() {
        Topic topic = new Topic(1L, "Space");
        Concept concept = new Concept(2L, topic, "Stars");
        Question question = new Question(11L, "What is a star?", concept, Difficulty.EASY, null, QuestionType.DEFINITION, null, null);

        LearningStateAssembler assembler = new LearningStateAssembler(new FakeQuestions(question));
        LearningSessionStore.LearningSession session = new LearningSessionStore.LearningSession(
                "u1", concept.id(), question.id(), LearningPhase.QUESTION, null, 0,
                new ArrayDeque<>(), new ArrayDeque<>(), new ArrayDeque<>(), 0
        );

        LearningState state = assembler.assemble("u1", session);

        assertEquals(LearningPhase.QUESTION, state.phase());
        assertInstanceOf(QuestionActivity.class, state.currentActivity());
        assertEquals(ActionType.SUBMIT_ANSWER, state.availableActions().get(0).type());
    }

    @Test
    void shouldBuildPracticeActivityState() {
        Topic topic = new Topic(1L, "Space");
        Concept concept = new Concept(2L, topic, "Stars");
        Question question = new Question(11L, "What is a star?", concept, Difficulty.EASY, null, QuestionType.DEFINITION, null, null);
        LearningCycle cycle = new LearningCycle(
                null,
                null,
                List.of(new PracticeItem(PracticeType.TRUE_FALSE, "Stars are hot", List.of(), List.of(), true, false)),
                List.of(),
                "retry"
        );

        LearningStateAssembler assembler = new LearningStateAssembler(new FakeQuestions(question));
        LearningSessionStore.LearningSession session = new LearningSessionStore.LearningSession(
                "u1", concept.id(), question.id(), LearningPhase.PRACTICE, cycle, 0,
                new ArrayDeque<>(), new ArrayDeque<>(), new ArrayDeque<>(), 2
        );

        LearningState state = assembler.assemble("u1", session);

        assertEquals(LearningPhase.PRACTICE, state.phase());
        PracticeActivity activity = assertInstanceOf(PracticeActivity.class, state.currentActivity());
        assertEquals(1, activity.currentItem());
        assertEquals(1, activity.totalItems());
        assertEquals(ActionType.SUBMIT_PRACTICE, state.availableActions().get(0).type());
    }

    private static class FakeQuestions implements GetQuestionUseCase {
        private final Question question;

        private FakeQuestions(Question question) {
            this.question = question;
        }

        @Override public Optional<Question> getNext(Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextInConcept(Long conceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextInMicroConcept(Long conceptId, Long microConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextFromNextMicroConcept(Long conceptId, Long currentMicroConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getNextFromNextConcept(Long currentConceptId, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getFirstFromTopic(String topicName, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getFirstFromConceptName(String conceptName, Difficulty preferredDifficulty, Set<Long> excludedQuestionIds) { return Optional.empty(); }
        @Override public Optional<Question> getById(Long id) { return Optional.ofNullable(question); }
        @Override public int conceptOrder(Long conceptId) { return 1; }
        @Override public int totalConcepts() { return 6; }
        @Override public int microConceptOrder(Long conceptId, Long microConceptId) { return 1; }
        @Override public int totalMicroConcepts(Long conceptId) { return 6; }
    }
}
