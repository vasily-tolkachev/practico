package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.application.dto.AuthenticationRequest;
import com.myproject.practico.auth.application.dto.AuthenticationResponse;

public interface AuthenticateUserUseCase {
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
