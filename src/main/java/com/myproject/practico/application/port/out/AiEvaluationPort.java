package com.myproject.practico.application.port.out;

import com.myproject.practico.application.service.AiRequest;
import com.myproject.practico.application.service.AiResponse;

public interface AiEvaluationPort {
    AiResponse evaluate(AiRequest request);
}
