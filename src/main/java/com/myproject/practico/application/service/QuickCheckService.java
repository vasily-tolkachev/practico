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
            return new QuickCheckResult(false, "Мини-проверка недоступна. Продолжайте следующим ответом.");
        }
        return quickCheckPort.evaluate(new QuickCheckRequest(
                quickCheck.question(),
                quickCheck.expectedAnswer(),
                userAnswer
        ));
    }
}
