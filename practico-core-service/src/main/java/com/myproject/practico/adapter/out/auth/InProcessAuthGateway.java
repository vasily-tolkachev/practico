package com.myproject.practico.adapter.out.auth;

import com.myproject.practico.auth.AuthGateway;
import com.myproject.practico.auth.dto.LoginCommand;
import com.myproject.practico.auth.dto.TokenPair;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"default", "inprocess-auth"})
public class InProcessAuthGateway implements AuthGateway {

    @Override
    public TokenPair login(LoginCommand command) {
        throw new UnsupportedOperationException("In-process auth is not enabled in core");
    }

    @Override
    public TokenPair refresh(String refreshToken) {
        throw new UnsupportedOperationException("Refresh is not available in in-process mode yet");
    }
}
