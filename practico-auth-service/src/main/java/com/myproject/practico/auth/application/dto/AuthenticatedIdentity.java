package com.myproject.practico.auth.application.dto;

public record AuthenticatedIdentity(
        String subject,
        String email,
        String displayName,
        String avatarUrl
) {
}
