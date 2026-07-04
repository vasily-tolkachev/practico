package com.myproject.practico.application.auth;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken
) {
}
