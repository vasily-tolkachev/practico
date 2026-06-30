package com.myproject.practico.application.learning.state;

import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.service.LearningCycle;
import com.myproject.practico.application.service.LearningPhase;
import com.myproject.practico.application.service.LearningSessionStore;
import com.myproject.practico.application.service.PracticeItem;
import com.myproject.practico.domain.Question;

import java.util.List;

public class LearningStateAssembler {

    private final GetQuestionUseCase getQuestionUseCase;

    public LearningStateAssembler(GetQuestionUseCase getQuestionUseCase) {
        this.getQuestionUseCase = getQuestionUseCase;
    }

    public LearningState assemble(String userId, LearningSessionStore.LearningSession session) {
        if (session == null) {
            return inactiveState(userId);
        }

        Question question = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
        LearningContext context = toContext(question);
        ProgressSnapshot progress = toProgress(question, session);
        LearningActivity currentActivity = toActivity(session.phase(), session.currentCycle(), question, session.currentPracticeIndex());
        List<AvailableAction> availableActions = actionsFor(session.phase());

        return new LearningState(
                SCHEMA_VERSION,
                session.userId(),
                userId,
                session.phase(),
                context,
                progress,
                currentActivity,
                availableActions
        );
    }

    private LearningState inactiveState(String userId) {
        return new LearningState(
                SCHEMA_VERSION,
                null,
                userId,
                LearningPhase.COMPLETED,
                new LearningContext(null, null, null, null, null, null),
                new ProgressSnapshot(null, getQuestionUseCase.totalConcepts(), null, null, 0),
                new CompletedActivity("inactive"),
                List.of(new AvailableAction(ActionType.START_LEARNING, true))
        );
    }

    private LearningContext toContext(Question question) {
        if (question == null) {
            return new LearningContext(null, null, null, null, null, null);
        }

        Long topicId = question.concept() != null && question.concept().topic() != null ? question.concept().topic().id() : null;
        String topicName = question.concept() != null && question.concept().topic() != null ? question.concept().topic().name() : null;
        Long conceptId = question.concept() == null ? null : question.concept().id();
        String conceptName = question.concept() == null ? null : question.concept().name();
        Long microConceptId = question.microConcept() == null ? null : question.microConcept().id();
        String microConceptName = question.microConcept() == null ? null : question.microConcept().name();

        return new LearningContext(topicId, topicName, conceptId, conceptName, microConceptId, microConceptName);
    }

    private ProgressSnapshot toProgress(Question question, LearningSessionStore.LearningSession session) {
        Integer conceptOrder = question == null || question.concept() == null || question.concept().id() == null
                ? null
                : getQuestionUseCase.conceptOrder(question.concept().id());
        Integer totalConcepts = getQuestionUseCase.totalConcepts();
        Integer microOrder = question == null
                || question.concept() == null
                || question.concept().id() == null
                || question.microConcept() == null
                || question.microConcept().id() == null
                ? null
                : getQuestionUseCase.microConceptOrder(question.concept().id(), question.microConcept().id());
        Integer totalMicro = question == null || question.concept() == null || question.concept().id() == null
                ? null
                : getQuestionUseCase.totalMicroConcepts(question.concept().id());

        return new ProgressSnapshot(conceptOrder, totalConcepts, microOrder, totalMicro, session.answeredCount());
    }

    private LearningActivity toActivity(
            LearningPhase phase,
            LearningCycle cycle,
            Question question,
            int currentPracticeIndex
    ) {
        return switch (phase) {
            case QUESTION -> new QuestionActivity(
                    question == null ? null : question.id(),
                    question == null ? null : question.text(),
                    question == null || question.difficulty() == null ? null : question.difficulty().name(),
                    question == null || question.questionType() == null ? null : question.questionType().name()
            );
            case LEARNING_CARD -> new LearningCardActivity(
                    cycle == null || cycle.learningCard() == null ? null : cycle.learningCard().title(),
                    cycle == null || cycle.learningCard() == null ? null : cycle.learningCard().explanation()
            );
            case PRACTICE -> toPracticeActivity(cycle, currentPracticeIndex);
            case QUICK_CHECK -> new QuickCheckActivity(
                    cycle == null || cycle.quickCheck() == null ? null : cycle.quickCheck().question()
            );
            case RETRY -> new RetryActivity(
                    cycle == null ? null : cycle.retryQuestion(),
                    cycle == null || cycle.retryRubric() == null ? List.of() : cycle.retryRubric()
            );
            case COMPLETED -> new CompletedActivity("completed");
        };
    }

    private PracticeActivity toPracticeActivity(LearningCycle cycle, int currentPracticeIndex) {
        List<PracticeItem> items = cycle == null || cycle.practiceItems() == null ? List.of() : cycle.practiceItems();
        Integer currentItem = items.isEmpty()
                ? null
                : Math.min(
                Math.max(0, currentPracticeIndex) + 1,
                items.size()
        );

        List<PracticeActivity.PracticeItemView> mappedItems = items.stream()
                .map(item -> new PracticeActivity.PracticeItemView(
                        item.type(),
                        item.question(),
                        item.options()
                ))
                .toList();

        return new PracticeActivity(currentItem, items.size(), mappedItems);
    }

    private List<AvailableAction> actionsFor(LearningPhase phase) {
        return LearningActionRules.allowedForPhase(phase).stream()
                .map(type -> new AvailableAction(type, true))
                .toList();
    }

    private static final int SCHEMA_VERSION = 1;
}
