package com.myproject.practico.application.learning.state;

import java.util.Objects;

public record AvailableAction(
        ActionType type,
        boolean enabled
) {
    public AvailableAction {
        Objects.requireNonNull(type, "type must not be null");
    }
}
