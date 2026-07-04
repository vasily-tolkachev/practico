package com.myproject.practico.application.port.in;

import com.myproject.practico.application.auth.AuthenticationRequest;
import com.myproject.practico.application.auth.AuthenticationResponse;

public interface AuthenticateUserUseCase {
    AuthenticationResponse authenticate(AuthenticationRequest request);
}
