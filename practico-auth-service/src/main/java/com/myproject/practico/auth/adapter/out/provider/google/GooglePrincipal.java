package com.myproject.practico.auth.adapter.out.provider.google;

public record GooglePrincipal(
        String subject,
        String email,
        String displayName,
        String avatarUrl
) {
}
