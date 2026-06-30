package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.adapter.in.rest.dto.LearningStateResponse;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.service.LearningCycle;
import com.myproject.practico.application.service.LearningPhase;
import com.myproject.practico.application.service.LearningSessionService;
import com.myproject.practico.application.service.LearningSessionStore;
import com.myproject.practico.application.service.PracticeItem;
import com.myproject.practico.domain.Question;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LearningStateMapper {

    private final LearningSessionService learningSessionService;
    private final GetQuestionUseCase getQuestionUseCase;

    public LearningStateMapper(
            LearningSessionService learningSessionService,
            GetQuestionUseCase getQuestionUseCase
    ) {
        this.learningSessionService = learningSessionService;
        this.getQuestionUseCase = getQuestionUseCase;
    }

    public LearningStateResponse toState(String userId) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return inactiveState(userId);
        }

        Question question = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
        LearningCycle cycle = session.currentCycle();

        LearningStateResponse.TopicView topic = question != null
                && question.concept() != null
                && question.concept().topic() != null
                ? new LearningStateResponse.TopicView(
                question.concept().topic().id(),
                question.concept().topic().name()
        )
                : null;

        LearningStateResponse.ConceptView concept = question != null && question.concept() != null
                ? new LearningStateResponse.ConceptView(
                question.concept().id(),
                question.concept().name()
        )
                : null;

        LearningStateResponse.MicroConceptView microConcept = question != null && question.microConcept() != null
                ? new LearningStateResponse.MicroConceptView(
                question.microConcept().id(),
                question.microConcept().name(),
                question.microConcept().sortOrder()
        )
                : null;

        Integer conceptOrder = concept == null || concept.id() == null ? null : getQuestionUseCase.conceptOrder(concept.id());
        Integer totalConcepts = getQuestionUseCase.totalConcepts();
        Integer microOrder = concept == null || concept.id() == null || microConcept == null || microConcept.id() == null
                ? null
                : getQuestionUseCase.microConceptOrder(concept.id(), microConcept.id());
        Integer totalMicroConcepts = concept == null || concept.id() == null
                ? null
                : getQuestionUseCase.totalMicroConcepts(concept.id());

        LearningStateResponse.ProgressView progress = new LearningStateResponse.ProgressView(
                conceptOrder,
                totalConcepts,
                microOrder,
                totalMicroConcepts,
                session.answeredCount()
        );

        LearningStateResponse.QuestionView questionView = question == null
                ? null
                : new LearningStateResponse.QuestionView(
                question.id(),
                question.text(),
                question.difficulty(),
                question.questionType()
        );

        LearningStateResponse.LearningCardView learningCard = cycle == null || cycle.learningCard() == null
                ? null
                : new LearningStateResponse.LearningCardView(
                cycle.learningCard().title(),
                cycle.learningCard().explanation()
        );

        LearningStateResponse.QuickCheckView quickCheck = cycle == null || cycle.quickCheck() == null
                ? null
                : new LearningStateResponse.QuickCheckView(
                cycle.quickCheck().question(),
                cycle.quickCheck().expectedAnswer()
        );

        List<PracticeItem> practiceItems = cycle == null || cycle.practiceItems() == null ? List.of() : cycle.practiceItems();
        List<LearningStateResponse.PracticeItemView> practiceItemViews = practiceItems.stream()
                .map(item -> new LearningStateResponse.PracticeItemView(
                        item.type(),
                        item.question(),
                        item.options() == null ? List.of() : item.options()
                ))
                .toList();

        Integer currentPracticeIndex = practiceItems.isEmpty()
                ? null
                : Math.min(Math.max(0, session.currentPracticeIndex()) + 1, practiceItems.size());
        LearningStateResponse.PracticeView practice = new LearningStateResponse.PracticeView(
                currentPracticeIndex,
                practiceItems.size(),
                practiceItemViews
        );

        LearningStateResponse.RetryView retry = cycle == null
                ? null
                : new LearningStateResponse.RetryView(
                cycle.retryQuestion(),
                cycle.retryRubric() == null ? List.of() : cycle.retryRubric()
        );

        return new LearningStateResponse(
                true,
                userId,
                session.phase(),
                topic,
                concept,
                microConcept,
                progress,
                questionView,
                learningCard,
                quickCheck,
                practice,
                retry
        );
    }

    public LearningStateResponse inactiveState(String userId) {
        return new LearningStateResponse(
                false,
                userId,
                LearningPhase.COMPLETED,
                null,
                null,
                null,
                new LearningStateResponse.ProgressView(null, getQuestionUseCase.totalConcepts(), null, null, 0),
                null,
                null,
                null,
                new LearningStateResponse.PracticeView(null, 0, List.of()),
                null
        );
    }
}
