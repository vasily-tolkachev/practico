package com.myproject.practico.auth;

import com.myproject.practico.auth.dto.LoginCommand;
import com.myproject.practico.auth.dto.TokenPair;

public interface AuthGateway {
    TokenPair login(LoginCommand command);
    TokenPair refresh(String refreshToken);
}
