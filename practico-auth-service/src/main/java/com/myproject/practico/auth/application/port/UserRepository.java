package com.myproject.practico.auth.application.port;

import com.myproject.practico.auth.domain.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    User save(User user);
}
