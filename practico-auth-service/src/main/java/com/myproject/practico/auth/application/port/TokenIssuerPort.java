package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.contract.TokenResponse;
import com.myproject.practico.auth.domain.User;

import java.util.UUID;

public interface TokenIssuerPort {
    TokenResponse issue(User user, UUID sessionId);
}
