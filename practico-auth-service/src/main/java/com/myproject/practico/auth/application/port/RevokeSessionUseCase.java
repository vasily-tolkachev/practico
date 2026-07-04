package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.application.dto.RevokeSessionRequest;

public interface RevokeSessionUseCase {
    void revoke(RevokeSessionRequest request);
}
