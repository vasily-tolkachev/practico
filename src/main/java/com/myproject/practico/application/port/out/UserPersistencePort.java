package com.myproject.practico.application.port.out;

import com.myproject.practico.domain.User;

import java.time.Instant;

public interface UserPersistencePort {
    User upsertByTelegramId(String telegramId, Instant seenAt);
}
