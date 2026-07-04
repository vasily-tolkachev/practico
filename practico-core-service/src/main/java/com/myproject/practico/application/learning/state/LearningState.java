package com.myproject.practico.application.learning.state;

import com.myproject.practico.application.service.LearningPhase;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record LearningState(
        int schemaVersion,
        String sessionId,
        String userId,
        LearningPhase phase,
        LearningContext context,
        ProgressSnapshot progress,
        LearningActivity currentActivity,
        List<AvailableAction> availableActions
) {
    public LearningState {
        if (schemaVersion <= 0) {
            throw new IllegalArgumentException("schemaVersion must be positive");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        Objects.requireNonNull(phase, "phase must not be null");
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(progress, "progress must not be null");
        Objects.requireNonNull(currentActivity, "currentActivity must not be null");
        availableActions = availableActions == null ? List.of() : List.copyOf(availableActions);

        validatePhaseActivityCompatibility(phase, currentActivity);
        validateActions(phase, availableActions);
        validateContext(context);
        validateProgress(progress);
    }

    private static void validatePhaseActivityCompatibility(LearningPhase phase, LearningActivity activity) {
        boolean matches = switch (phase) {
            case QUESTION -> activity instanceof QuestionActivity;
            case LEARNING_CARD -> activity instanceof LearningCardActivity;
            case PRACTICE -> activity instanceof PracticeActivity;
            case QUICK_CHECK -> activity instanceof QuickCheckActivity;
            case RETRY -> activity instanceof RetryActivity;
            case COMPLETED -> activity instanceof CompletedActivity;
        };
        if (!matches) {
            throw new IllegalArgumentException("currentActivity does not match phase");
        }
    }

    private static void validateActions(LearningPhase phase, List<AvailableAction> actions) {
        Set<ActionType> allowed = LearningActionRules.allowedForPhase(phase);
        for (AvailableAction action : actions) {
            if (!allowed.contains(action.type())) {
                throw new IllegalArgumentException("availableActions contains action not allowed for phase");
            }
        }
        boolean hasEnabledAllowed = actions.stream()
                .anyMatch(action -> action.enabled() && allowed.contains(action.type()));
        if (!hasEnabledAllowed) {
            throw new IllegalArgumentException("availableActions must include at least one enabled action for phase");
        }
    }

    private static void validateContext(LearningContext context) {
        boolean hasTopic = context.topicId() != null || notBlank(context.topicName());
        boolean hasConcept = context.conceptId() != null || notBlank(context.conceptName());
        boolean hasMicro = context.microConceptId() != null || notBlank(context.microConceptName());

        if (hasMicro && !hasConcept) {
            throw new IllegalArgumentException("microConcept requires concept in context");
        }
        if (hasConcept && !hasTopic) {
            throw new IllegalArgumentException("concept requires topic in context");
        }
    }

    private static void validateProgress(ProgressSnapshot progress) {
        if (progress.answeredCount() == null || progress.answeredCount() < 0) {
            throw new IllegalArgumentException("answeredCount must be non-negative");
        }
        if (progress.conceptOrder() != null) {
            if (progress.totalConcepts() == null || progress.totalConcepts() <= 0) {
                throw new IllegalArgumentException("conceptOrder requires positive totalConcepts");
            }
            if (progress.conceptOrder() <= 0 || progress.conceptOrder() > progress.totalConcepts()) {
                throw new IllegalArgumentException("conceptOrder must be within totalConcepts");
            }
        }
        if (progress.microConceptOrder() != null) {
            if (progress.totalMicroConcepts() == null || progress.totalMicroConcepts() <= 0) {
                throw new IllegalArgumentException("microConceptOrder requires positive totalMicroConcepts");
            }
            if (progress.microConceptOrder() <= 0 || progress.microConceptOrder() > progress.totalMicroConcepts()) {
                throw new IllegalArgumentException("microConceptOrder must be within totalMicroConcepts");
            }
        }
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
