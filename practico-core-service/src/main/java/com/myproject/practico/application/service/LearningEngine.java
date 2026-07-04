package com.myproject.practico.application.service;

import com.myproject.practico.domain.Question;

import java.time.Instant;
import java.util.UUID;

public interface LearningEngine {

    LearningResult handleQuestionAnswer(
            UUID userId,
            Question currentQuestion,
            String answer,
            LearningSessionStore.LearningSession session,
            Instant now
    );

    LearningResult handleRetryAnswer(
            UUID userId,
            Question currentQuestion,
            String answer,
            LearningSessionStore.LearningSession session,
            Instant now
    );
}
