package com.myproject.practico.application.service;

public record QuickCheckResult(
        boolean correct,
        QuickCheckFeedbackCode feedbackCode
) {
    public QuickCheckResult(boolean correct, String ignoredFeedback) {
        this(correct, correct ? QuickCheckFeedbackCode.CORRECT : QuickCheckFeedbackCode.INCORRECT);
    }
}
