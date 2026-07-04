package com.myproject.practico.adapter.in.rest;

import com.myproject.practico.application.auth.AuthenticationRequest;
import com.myproject.practico.application.auth.AuthenticationResponse;
import com.myproject.practico.application.port.in.AuthenticateUserUseCase;
import com.myproject.practico.domain.AuthenticationProviderType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(AuthenticateUserUseCase authenticateUserUseCase) {
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequest request) {
        if (request == null || request.provider() == null || request.providerToken() == null || request.providerToken().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            AuthenticationProviderType provider = AuthenticationProviderType.valueOf(request.provider().trim().toUpperCase());
            AuthenticationResponse response = authenticateUserUseCase.authenticate(
                    new AuthenticationRequest(provider, request.providerToken())
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    public record LoginRequest(String provider, String providerToken) {
    }
}
