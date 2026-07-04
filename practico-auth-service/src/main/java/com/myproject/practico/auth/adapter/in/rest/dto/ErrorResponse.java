package com.myproject.practico.auth.adapter.in.rest.dto;

import com.myproject.practico.auth.contract.ErrorCode;

public record ErrorResponse(
        ErrorCode code,
        String message
) {
}
