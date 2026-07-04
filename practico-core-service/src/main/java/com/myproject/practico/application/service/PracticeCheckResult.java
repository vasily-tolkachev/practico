package com.myproject.practico.application.service;

public record PracticeCheckResult(
        boolean correct,
        PracticeFeedbackCode feedbackCode
) {
}
