package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.UserJpaEntity;
import com.myproject.practico.application.port.out.UserPersistencePort;
import com.myproject.practico.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User upsertByTelegramId(String telegramId, Instant seenAt) {
        UserJpaEntity existing = userJpaRepository.findByTelegramId(telegramId).orElse(null);
        if (existing == null) {
            UserJpaEntity created = userJpaRepository.save(new UserJpaEntity(
                    null,
                    telegramId,
                    seenAt,
                    seenAt
            ));
            return toDomain(created);
        }

        existing.setLastSeen(seenAt);
        UserJpaEntity updated = userJpaRepository.save(existing);
        return toDomain(updated);
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getTelegramId(),
                entity.getCreatedAt(),
                entity.getLastSeen()
        );
    }
}
