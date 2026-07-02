package com.myproject.practico.domain;

import java.time.Instant;

public record RuntimeContext(
        Long goalId,
        String programId,
        Instant boundAt
) {
}
