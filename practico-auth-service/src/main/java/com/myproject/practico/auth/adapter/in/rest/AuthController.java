package com.myproject.practico.auth.adapter.in.rest;

import com.myproject.practico.auth.adapter.in.rest.dto.LoginRequest;
import com.myproject.practico.auth.adapter.in.rest.dto.RefreshRequest;
import com.myproject.practico.auth.application.dto.AuthenticationRequest;
import com.myproject.practico.auth.application.dto.AuthenticationResponse;
import com.myproject.practico.auth.application.dto.RefreshTokenRequest;
import com.myproject.practico.auth.application.port.AuthenticateUserUseCase;
import com.myproject.practico.auth.application.port.RefreshTokenUseCase;
import com.myproject.practico.auth.contract.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(
            AuthenticateUserUseCase authenticateUserUseCase,
            RefreshTokenUseCase refreshTokenUseCase
    ) {
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResponse response = authenticateUserUseCase.authenticate(
                new AuthenticationRequest(request.provider(), request.providerToken())
        );
        return response.tokens();
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        AuthenticationResponse response = refreshTokenUseCase.refresh(
                new RefreshTokenRequest(request.refreshToken())
        );
        return response.tokens();
    }
}
