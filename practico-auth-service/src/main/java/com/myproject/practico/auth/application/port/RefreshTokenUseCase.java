package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.application.dto.RefreshTokenRequest;
import com.myproject.practico.auth.application.dto.AuthenticationResponse;

public interface RefreshTokenUseCase {
    AuthenticationResponse refresh(RefreshTokenRequest request);
}
