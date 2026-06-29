package com.myproject.practico.application.service;

import com.myproject.practico.domain.UserConceptProgress;

public class DefaultRetryMasteryPolicy implements RetryMasteryPolicy {

    @Override
    public boolean isMastered(EvaluationResult evaluation, UserConceptProgress progress) {
        return evaluation.score() >= 8;
    }
}
