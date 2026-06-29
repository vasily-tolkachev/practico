package com.myproject.practico.application.port.out;

import com.myproject.practico.application.service.QuickCheckRequest;
import com.myproject.practico.application.service.QuickCheckResult;

public interface QuickCheckPort {
    QuickCheckResult evaluate(QuickCheckRequest request);
}
