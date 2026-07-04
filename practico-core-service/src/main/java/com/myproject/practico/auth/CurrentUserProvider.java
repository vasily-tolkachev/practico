package com.myproject.practico.auth;

import java.util.Optional;
import java.util.UUID;

public interface CurrentUserProvider {
    Optional<UUID> currentUserId();
}
