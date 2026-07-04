package com.myproject.practico.application.service;

import com.myproject.practico.application.port.out.EvaluationPort;
import com.myproject.practico.domain.QuestionType;

import java.util.List;

public class EvaluationService {

    private final EvaluationPort evaluationPort;

    public EvaluationService(EvaluationPort evaluationPort) {
        this.evaluationPort = evaluationPort;
    }

    public EvaluationResult evaluate(String question, String answer, QuestionType questionType) {
        return evaluationPort.evaluate(new EvaluationRequest(question, answer, questionType, List.of()));
    }

    public EvaluationResult evaluateRetry(String question, String answer, QuestionType questionType, List<String> retryRubric) {
        return evaluationPort.evaluate(new EvaluationRequest(question, answer, questionType, retryRubric == null ? List.of() : retryRubric));
    }
}
