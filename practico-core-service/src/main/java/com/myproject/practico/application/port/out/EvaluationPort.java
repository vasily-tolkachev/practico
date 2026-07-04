package com.myproject.practico.application.port.out;

import com.myproject.practico.application.service.EvaluationRequest;
import com.myproject.practico.application.service.EvaluationResult;

public interface EvaluationPort {
    EvaluationResult evaluate(EvaluationRequest request);
}
