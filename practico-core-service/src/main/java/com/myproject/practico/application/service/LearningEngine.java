package com.myproject.practico.application.service;

import com.myproject.practico.domain.Question;

import java.time.Instant;

public interface LearningEngine {

    LearningResult handleQuestionAnswer(
            Long userId,
            Question currentQuestion,
            String answer,
            LearningSessionStore.LearningSession session,
            Instant now
    );

    LearningResult handleRetryAnswer(
            Long userId,
            Question currentQuestion,
            String answer,
            LearningSessionStore.LearningSession session,
            Instant now
    );
}
