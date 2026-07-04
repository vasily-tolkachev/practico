package com.myproject.practico.adapter.out.persistence;

import com.myproject.practico.adapter.out.persistence.entity.UserJpaEntity;
import com.myproject.practico.application.port.out.UserPersistencePort;
import com.myproject.practico.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User create(String displayName, Instant now) {
        UserJpaEntity created = userJpaRepository.save(new UserJpaEntity(
                null,
                displayName,
                now,
                now
        ));
        return toDomain(created);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return userJpaRepository.findById(userId).map(this::toDomain);
    }

    @Override
    public User touch(Long userId, Instant now) {
        UserJpaEntity existing = userJpaRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found: " + userId));
        existing.setUpdatedAt(now);
        return toDomain(userJpaRepository.save(existing));
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getDisplayName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
