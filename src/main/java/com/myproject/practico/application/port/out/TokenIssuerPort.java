package com.myproject.practico.application.port.out;

import com.myproject.practico.application.auth.AuthenticationResponse;

public interface TokenIssuerPort {
    AuthenticationResponse issueTokens(Long userId);
}
