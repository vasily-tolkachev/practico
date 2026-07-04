package com.myproject.practico.auth.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
