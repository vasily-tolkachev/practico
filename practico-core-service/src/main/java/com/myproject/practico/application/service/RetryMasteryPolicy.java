package com.myproject.practico.application.service;

import com.myproject.practico.domain.UserConceptProgress;

public interface RetryMasteryPolicy {

    boolean isMastered(EvaluationResult evaluation, UserConceptProgress progress, boolean quickCheckPassed);
}
