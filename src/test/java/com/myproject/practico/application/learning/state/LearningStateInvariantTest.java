package com.myproject.practico.application.learning.state;

import com.myproject.practico.application.service.LearningPhase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LearningStateInvariantTest {

    @Test
    void validStateShouldPass() {
        assertDoesNotThrow(() -> new LearningState(
                1,
                "session-1",
                "user-1",
                LearningPhase.QUESTION,
                new LearningContext(1L, "topic", 2L, "concept", 3L, "micro"),
                new ProgressSnapshot(1, 3, 1, 6, 0),
                new QuestionActivity(10L, "q", "EASY", "DEFINITION"),
                List.of(new AvailableAction(ActionType.SUBMIT_ANSWER, true))
        ));
    }

    @Test
    void phaseActivityMismatchShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> new LearningState(
                1,
                "session-1",
                "user-1",
                LearningPhase.PRACTICE,
                new LearningContext(1L, "topic", 2L, "concept", 3L, "micro"),
                new ProgressSnapshot(1, 3, 1, 6, 0),
                new QuestionActivity(10L, "q", "EASY", "DEFINITION"),
                List.of(new AvailableAction(ActionType.SUBMIT_PRACTICE, true))
        ));
    }

    @Test
    void invalidActionForPhaseShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> new LearningState(
                1,
                "session-1",
                "user-1",
                LearningPhase.QUESTION,
                new LearningContext(1L, "topic", 2L, "concept", 3L, "micro"),
                new ProgressSnapshot(1, 3, 1, 6, 0),
                new QuestionActivity(10L, "q", "EASY", "DEFINITION"),
                List.of(new AvailableAction(ActionType.CONTINUE_LEARNING, true))
        ));
    }

    @Test
    void inconsistentContextShouldFail() {
        assertThrows(IllegalArgumentException.class, () -> new LearningState(
                1,
                "session-1",
                "user-1",
                LearningPhase.QUESTION,
                new LearningContext(null, null, null, null, 3L, "micro"),
                new ProgressSnapshot(1, 3, 1, 6, 0),
                new QuestionActivity(10L, "q", "EASY", "DEFINITION"),
                List.of(new AvailableAction(ActionType.SUBMIT_ANSWER, true))
        ));
    }
}
