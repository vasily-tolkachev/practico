package com.myproject.practico.adapter.out.auth;

import com.myproject.practico.application.auth.AuthenticationRequest;
import com.myproject.practico.application.auth.AuthenticationResponse;
import com.myproject.practico.application.port.in.AuthenticateUserUseCase;
import com.myproject.practico.auth.AuthGateway;
import com.myproject.practico.auth.dto.LoginCommand;
import com.myproject.practico.auth.dto.TokenPair;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile({"default", "inprocess-auth"})
public class InProcessAuthGateway implements AuthGateway {

    private final AuthenticateUserUseCase authenticateUserUseCase;

    public InProcessAuthGateway(AuthenticateUserUseCase authenticateUserUseCase) {
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @Override
    public TokenPair login(LoginCommand command) {
        AuthenticationResponse response = authenticateUserUseCase.authenticate(
                new AuthenticationRequest(command.provider(), command.providerToken())
        );
        return new TokenPair(response.accessToken(), response.refreshToken());
    }

    @Override
    public TokenPair refresh(String refreshToken) {
        throw new UnsupportedOperationException("Refresh is not available in in-process mode yet");
    }
}
