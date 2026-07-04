package com.myproject.practico.auth.dto;

public record LoginCommand(
        String provider,
        String providerToken
) {
}
