package com.myproject.practico.application.learning.state;

import com.myproject.practico.application.service.LearningPhase;

import java.util.Set;

public final class LearningActionRules {

    private LearningActionRules() {
    }

    public static Set<ActionType> allowedForPhase(LearningPhase phase) {
        return switch (phase) {
            case QUESTION -> Set.of(ActionType.SUBMIT_ANSWER, ActionType.CONTINUE_LEARNING);
            case LEARNING_CARD -> Set.of(ActionType.CONTINUE_LEARNING);
            case PRACTICE -> Set.of(ActionType.SUBMIT_PRACTICE);
            case QUICK_CHECK -> Set.of(ActionType.SUBMIT_QUICK_CHECK);
            case RETRY -> Set.of(ActionType.SUBMIT_RETRY);
            case COMPLETED -> Set.of(ActionType.START_LEARNING);
        };
    }
}
