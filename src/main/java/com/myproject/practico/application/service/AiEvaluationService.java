package com.myproject.practico.application.service;

import com.myproject.practico.application.port.out.AiEvaluationPort;

public class AiEvaluationService {

    private final AiEvaluationPort aiEvaluationPort;

    public AiEvaluationService(AiEvaluationPort aiEvaluationPort) {
        this.aiEvaluationPort = aiEvaluationPort;
    }

    public AiResponse evaluate(String question, String answer) {
        return aiEvaluationPort.evaluate(new AiRequest(question, answer));
    }
}
