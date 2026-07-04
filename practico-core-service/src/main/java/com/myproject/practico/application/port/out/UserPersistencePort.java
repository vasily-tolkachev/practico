package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.User;

import java.time.Instant;
import java.util.Optional;

public interface UserPersistencePort {
    User create(String displayName, Instant now);
    Optional<User> findById(Long userId);
    User touch(Long userId, Instant now);
}
