package com.myproject.practico.application.service;

import com.myproject.practico.application.port.out.EvaluationPort;

public class EvaluationService {

    private final EvaluationPort evaluationPort;

    public EvaluationService(EvaluationPort evaluationPort) {
        this.evaluationPort = evaluationPort;
    }

    public EvaluationResult evaluate(String question, String answer) {
        return evaluationPort.evaluate(new EvaluationRequest(question, answer));
    }
}
