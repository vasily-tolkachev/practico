package com.myproject.practico.application.service;

import com.myproject.practico.application.port.out.QuickCheckPort;
import com.myproject.practico.domain.QuickCheck;

public class QuickCheckService {

    private final QuickCheckPort quickCheckPort;

    public QuickCheckService(QuickCheckPort quickCheckPort) {
        this.quickCheckPort = quickCheckPort;
    }

    public QuickCheckResult check(String userAnswer, QuickCheck quickCheck) {
        if (quickCheck == null || quickCheck.question() == null || quickCheck.question().isBlank()) {
            return new QuickCheckResult(false, QuickCheckFeedbackCode.UNAVAILABLE);
        }
        QuickCheckResult result = quickCheckPort.evaluate(new QuickCheckRequest(
                quickCheck.question(),
                quickCheck.expectedAnswer(),
                userAnswer
        ));
        if (result.correct()) {
            return new QuickCheckResult(true, QuickCheckFeedbackCode.CORRECT);
        }
        return new QuickCheckResult(false, QuickCheckFeedbackCode.INCORRECT);
    }
}
