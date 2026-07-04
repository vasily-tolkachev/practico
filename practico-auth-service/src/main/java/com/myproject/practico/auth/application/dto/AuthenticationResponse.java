package com.myproject.practico.auth.application.dto;

import com.myproject.practico.auth.contract.TokenResponse;

public record AuthenticationResponse(
        TokenResponse tokens
) {
}
