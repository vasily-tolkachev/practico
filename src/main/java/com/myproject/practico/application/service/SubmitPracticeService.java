package com.myproject.practico.application.service;

import com.myproject.practico.application.learning.state.LearningState;
import com.myproject.practico.application.learning.state.LearningStateAssembler;
import com.myproject.practico.application.port.in.GetQuestionUseCase;
import com.myproject.practico.application.port.in.SubmitPracticeUseCase;
import com.myproject.practico.domain.Difficulty;
import com.myproject.practico.domain.Question;

import java.util.List;

public class SubmitPracticeService implements SubmitPracticeUseCase {

    private final LearningSessionService learningSessionService;
    private final PracticeService practiceService;
    private final GetQuestionUseCase getQuestionUseCase;
    private final LearningStateAssembler learningStateAssembler;

    public SubmitPracticeService(
            LearningSessionService learningSessionService,
            PracticeService practiceService,
            GetQuestionUseCase getQuestionUseCase,
            LearningStateAssembler learningStateAssembler
    ) {
        this.learningSessionService = learningSessionService;
        this.practiceService = practiceService;
        this.getQuestionUseCase = getQuestionUseCase;
        this.learningStateAssembler = learningStateAssembler;
    }

    @Override
    public LearningState submitPractice(String userId, PracticeAnswer answer) {
        LearningSessionStore.LearningSession session = learningSessionService.getSession(userId).orElse(null);
        if (session == null) {
            return learningStateAssembler.assemble(userId, null);
        }

        List<PracticeItem> practiceItems = session.currentCycle() == null ? null : session.currentCycle().practiceItems();
        if (practiceItems == null || practiceItems.isEmpty()) {
            return learningStateAssembler.assemble(userId, session);
        }

        int index = Math.max(0, session.currentPracticeIndex());
        if (index >= practiceItems.size()) {
            index = practiceItems.size() - 1;
        }

        PracticeItem currentItem = practiceItems.get(index);
        PracticeCheckResult checkResult = practiceService.check(answer, currentItem);
        if (!checkResult.correct()) {
            return learningStateAssembler.assemble(userId, session);
        }

        int nextIndex = index + 1;
        if (nextIndex < practiceItems.size()) {
            learningSessionService.setPracticeIndex(userId, nextIndex);
            return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(session));
        }

        learningSessionService.setPhase(userId, LearningPhase.RETRY);

        Question currentQuestion = getQuestionUseCase.getById(session.currentQuestionId()).orElse(null);
        if (currentQuestion != null && (session.currentCycle() == null || session.currentCycle().retryQuestion() == null || session.currentCycle().retryQuestion().isBlank())) {
            if (currentQuestion.concept() != null
                    && currentQuestion.concept().id() != null
                    && currentQuestion.microConcept() != null
                    && currentQuestion.microConcept().id() != null) {
                Question candidate = getQuestionUseCase
                        .getNextInMicroConcept(
                                currentQuestion.concept().id(),
                                currentQuestion.microConcept().id(),
                                Difficulty.MEDIUM,
                                learningSessionService.excludedQuestionIds(session)
                        )
                        .orElse(null);
                if (candidate != null && candidate.id() != null) {
                    learningSessionService.setCurrentQuestion(userId, currentQuestion.concept().id(), candidate.id());
                }
            }
        }

        return learningStateAssembler.assemble(userId, learningSessionService.getSession(userId).orElse(session));
    }
}
